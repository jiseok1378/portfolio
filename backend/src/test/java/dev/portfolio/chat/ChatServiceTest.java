package dev.portfolio.chat;

import com.google.genai.errors.ApiException;
import dev.portfolio.content.ContentRepository;
import dev.portfolio.rag.VectorStoreProvider;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private final ChatModel chatModel = mock(ChatModel.class);
    private final VectorStore store = mock(VectorStore.class);
    private final VectorStoreProvider provider = mock(VectorStoreProvider.class);
    private final ContentRepository content = new ContentRepository(JsonMapper.builder().build());
    private final ChatService service = new ChatService(chatModel, provider, new PromptFactory(), content, Duration.ofSeconds(5));

    private static Document doc(String slug, String title) {
        return new Document("[" + title + "] 해결\n본문", Map.of("slug", slug, "title", title));
    }

    private static ChatResponse chunk(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    private List<ChatEvent> run(String message) {
        return service.answer(new ChatRequest(message, List.of(), null)).collectList().block(Duration.ofSeconds(10));
    }

    private void givenDocs(List<Document> docs) {
        when(provider.get()).thenReturn(Optional.of(store));
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(docs);
    }

    @Test
    void streamsTokens_thenSources_thenDone() {
        givenDocs(List.of(doc("realtime-records", "실시간 경기 기록 파이프라인")));
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk("이 개발자는 "), chunk("DLQ를 두었습니다.")));

        List<ChatEvent> events = run("DLQ?");

        assertThat(events).extracting(ChatEvent::event).containsExactly("token", "token", "sources", "done");
        assertThat(events.get(0).data()).isEqualTo("이 개발자는 ");
    }

    @Test
    void sources_comeFromRetrievedDocsInOrder_deduplicated_skippingCareer() {
        givenDocs(List.of(
                doc("parallel-testing", "병렬"), doc("career", "경력 요약"),
                doc("realtime-records", "실시간"), doc("parallel-testing", "병렬")));
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk("답")));

        ChatEvent sources = run("q").stream().filter(e -> e.event().equals("sources")).findFirst().orElseThrow();

        assertThat(sources.data()).isEqualTo(List.of(
                Map.of("slug", "parallel-testing", "title", "병렬 처리로 테스트 생성 시간 4~8배 단축"),
                Map.of("slug", "realtime-records", "title", "실시간 경기 기록 파이프라인")));
    }

    @Test
    void noRetrievedDocs_stillAnswers_andSourcesEmpty() {
        givenDocs(List.of());
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk("그 내용은 자료에 없습니다.")));

        List<ChatEvent> events = run("날씨?");

        assertThat(events).extracting(ChatEvent::event).containsExactly("token", "sources", "done");
        assertThat(events.get(1).data()).isEqualTo(List.of());
    }

    @Test
    void vectorStoreMissing_emitsUnavailableError() {
        when(provider.get()).thenReturn(Optional.empty());

        assertThat(run("q")).containsExactly(ChatEvent.error(ChatErrorCode.UNAVAILABLE));
    }

    @Test
    void retrievalFailure_emitsUnavailableError() {
        when(provider.get()).thenReturn(Optional.of(store));
        when(store.similaritySearch(any(SearchRequest.class))).thenThrow(new IllegalStateException("embedding failed"));

        assertThat(run("q")).containsExactly(ChatEvent.error(ChatErrorCode.UNAVAILABLE));
    }

    @Test
    void upstream429MidStream_keepsSentTokens_emitsDailyLimitError() {
        givenDocs(List.of(doc("realtime-records", "실시간")));
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.concat(
                Flux.just(chunk("부분")),
                Flux.error(new RuntimeException("wrapped", new ApiException(429, "RESOURCE_EXHAUSTED", "quota")))));

        List<ChatEvent> events = run("q");

        assertThat(events).containsExactly(ChatEvent.token("부분"), ChatEvent.error(ChatErrorCode.DAILY_LIMIT));
    }

    @Test
    void retrieval429_emitsDailyLimitError() {
        when(provider.get()).thenReturn(Optional.of(store));
        when(store.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException(new ApiException(429, "RESOURCE_EXHAUSTED", "quota")));

        assertThat(run("q")).containsExactly(ChatEvent.error(ChatErrorCode.DAILY_LIMIT));
    }

    @Test
    void upstreamTimeout_emitsUnavailableError() {
        givenDocs(List.of());
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.error(new TimeoutException()));

        assertThat(run("q")).containsExactly(ChatEvent.error(ChatErrorCode.UNAVAILABLE));
    }

    @Test
    void slowUpstream_isCutByTimeout() {
        ChatService fast = new ChatService(chatModel, provider, new PromptFactory(), content, Duration.ofMillis(200));
        givenDocs(List.of());
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.never());

        List<ChatEvent> events = fast.answer(new ChatRequest("q", List.of(), null)).collectList().block(Duration.ofSeconds(5));

        assertThat(events).containsExactly(ChatEvent.error(ChatErrorCode.UNAVAILABLE));
    }
}
