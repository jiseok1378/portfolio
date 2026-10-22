package dev.portfolio.chat;

import dev.portfolio.quota.ClientKeyResolver;
import dev.portfolio.quota.QuestionQuota;
import dev.portfolio.quota.QuotaDecision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ChatService chatService;

    @MockitoBean
    QuestionQuota quota;

    @MockitoBean
    ClientKeyResolver clientKeyResolver;

    private String call(String json) throws Exception {
        MvcResult started = mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(request().asyncStarted())
                .andReturn();
        return mvc.perform(asyncDispatch(started))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(header().string("X-Accel-Buffering", "no"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void allowedQuestion_streamsServiceEvents() throws Exception {
        when(clientKeyResolver.resolve(any())).thenReturn("1.2.3.4");
        when(quota.tryConsume(any(), any())).thenReturn(QuotaDecision.ALLOWED);
        when(chatService.answer(any())).thenReturn(Flux.just(ChatEvent.token("안녕"), ChatEvent.done()));

        String body = call("{\"message\":\"안녕?\",\"history\":[]}");

        assertThat(body).contains("event:token", "data:\"안녕\"", "event:done");
    }

    @Test
    void tokenWithLeadingSpace_isSentAsJsonString_soTheSpaceSurvives() throws Exception {
        when(clientKeyResolver.resolve(any())).thenReturn("1.2.3.4");
        when(quota.tryConsume(any(), any())).thenReturn(QuotaDecision.ALLOWED);
        when(chatService.answer(any())).thenReturn(Flux.just(ChatEvent.token(" 남겨서\n다음 줄"), ChatEvent.done()));

        String body = call("{\"message\":\"q\"}");

        assertThat(body).contains("data:\" 남겨서\\n다음 줄\"");
    }

    @Test
    void message501Chars_isTooLongError_andDoesNotConsumeQuota() throws Exception {
        String body = call("{\"message\":\"" + "가".repeat(501) + "\",\"history\":[]}");

        assertThat(body).contains("event:error", "TOO_LONG", "질문은 500자 이내로 입력해 주세요.");
        verify(quota, never()).tryConsume(any(), any());
    }

    @Test
    void blankMessage_isEmptyError() throws Exception {
        String body = call("{\"message\":\"   \",\"history\":[]}");

        assertThat(body).contains("event:error", "EMPTY");
        verify(quota, never()).tryConsume(any(), any());
    }

    @Test
    void minuteLimit_returnsSseErrorEvent_notHttp429() throws Exception {
        when(clientKeyResolver.resolve(any())).thenReturn("1.2.3.4");
        when(quota.tryConsume(any(), any())).thenReturn(QuotaDecision.MINUTE_LIMIT);

        String body = call("{\"message\":\"q\",\"history\":[]}");

        assertThat(body).contains("event:error", "MINUTE_LIMIT", "질문이 너무 빠릅니다. 잠시 후 다시 시도해 주세요.");
        verify(chatService, never()).answer(any());
    }

    @Test
    void dailyLimit_returnsDailyLimitMessage() throws Exception {
        when(clientKeyResolver.resolve(any())).thenReturn("1.2.3.4");
        when(quota.tryConsume(any(), any())).thenReturn(QuotaDecision.DAILY_LIMIT);

        String body = call("{\"message\":\"q\"}");

        assertThat(body).contains("DAILY_LIMIT", "오늘 질문 한도가 끝났습니다. 내일 다시 질문해 주세요.");
    }
}
