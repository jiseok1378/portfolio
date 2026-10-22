package dev.portfolio.chat;

import com.google.genai.errors.ApiException;
import dev.portfolio.content.CaseContent;
import dev.portfolio.content.ContentRepository;
import dev.portfolio.rag.RagDocumentFactory;
import dev.portfolio.rag.VectorStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 검색 → 프롬프트 → 모델 스트리밍 → 근거 링크 순서로 답한다.
 * 근거 링크는 모델 답변이 아니라 서버가 검색한 문서로 정한다.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int TOP_K = 4;

    private final ChatModel chatModel;
    private final VectorStoreProvider vectorStore;
    private final PromptFactory promptFactory;
    private final ContentRepository content;
    private final Duration timeout;

    @Autowired
    public ChatService(ChatModel chatModel, VectorStoreProvider vectorStore, PromptFactory promptFactory,
                       ContentRepository content) {
        this(chatModel, vectorStore, promptFactory, content, Duration.ofSeconds(60));
    }

    ChatService(ChatModel chatModel, VectorStoreProvider vectorStore, PromptFactory promptFactory,
                ContentRepository content, Duration timeout) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.promptFactory = promptFactory;
        this.content = content;
        this.timeout = timeout;
    }

    public Flux<ChatEvent> answer(ChatRequest request) {
        Optional<VectorStore> store = vectorStore.get();
        if (store.isEmpty()) {
            return Flux.just(ChatEvent.error(ChatErrorCode.UNAVAILABLE));
        }

        List<Document> docs;
        try {
            docs = store.get().similaritySearch(SearchRequest.builder().query(request.message()).topK(TOP_K).build());
        } catch (RuntimeException e) {
            log.warn("자료 검색 실패", e);
            return Flux.just(ChatEvent.error(toErrorCode(e)));
        }

        String caseTitle = request.caseSlug() == null ? null
                : content.findCase(request.caseSlug()).map(CaseContent::title).orElse(null);

        Flux<ChatEvent> tokens = chatModel.stream(promptFactory.build(request.message(), request.history(), docs, caseTitle))
                .map(ChatService::text)
                .filter(text -> !text.isEmpty())
                .map(ChatEvent::token);

        return tokens
                .concatWith(Flux.just(ChatEvent.sources(sources(docs)), ChatEvent.done()))
                .timeout(timeout)
                .onErrorResume(e -> {
                    log.warn("답변 생성 실패", e);
                    return Flux.just(ChatEvent.error(toErrorCode(e)));
                });
    }

    private List<Map<String, String>> sources(List<Document> docs) {
        Map<String, String> bySlug = new LinkedHashMap<>();
        for (Document doc : docs) {
            Object slug = doc.getMetadata().get("slug");
            if (slug == null || RagDocumentFactory.CAREER_SLUG.equals(slug)) {
                continue;
            }
            content.findCase(slug.toString()).ifPresent(c -> bySlug.putIfAbsent(c.slug(), c.title()));
        }
        return bySlug.entrySet().stream().map(e -> Map.of("slug", e.getKey(), "title", e.getValue())).toList();
    }

    private static String text(ChatResponse response) {
        if (response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    /** Gemini 한도 초과(429)는 하루 한도 안내로, 그 밖의 실패는 사용 불가로 바꾼다. */
    static ChatErrorCode toErrorCode(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ApiException api && api.code() == 429) {
                return ChatErrorCode.DAILY_LIMIT;
            }
        }
        return ChatErrorCode.UNAVAILABLE;
    }
}
