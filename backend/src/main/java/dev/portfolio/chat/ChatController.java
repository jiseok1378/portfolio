package dev.portfolio.chat;

import dev.portfolio.quota.ClientKeyResolver;
import dev.portfolio.quota.QuestionQuota;
import dev.portfolio.quota.QuotaDecision;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

/** 오류도 HTTP 200 의 SSE error 이벤트로 보내, 화면이 한 경로로 처리하게 한다. */
@RestController
public class ChatController {

    static final int MAX_LENGTH = 500;

    private final ChatService chatService;
    private final QuestionQuota quota;
    private final ClientKeyResolver clientKeyResolver;
    private final ObjectMapper mapper;

    public ChatController(ChatService chatService, QuestionQuota quota, ClientKeyResolver clientKeyResolver,
                          ObjectMapper mapper) {
        this.chatService = chatService;
        this.quota = quota;
        this.clientKeyResolver = clientKeyResolver;
        this.mapper = mapper;
    }

    @PostMapping(path = "/api/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        httpResponse.setHeader("Cache-Control", "no-cache");
        httpResponse.setHeader("X-Accel-Buffering", "no");
        return events(request, httpRequest).map(this::toSse);
    }

    private Flux<ChatEvent> events(ChatRequest request, HttpServletRequest httpRequest) {
        String message = request.message() == null ? "" : request.message().strip();
        if (message.isEmpty()) {
            return Flux.just(ChatEvent.error(ChatErrorCode.EMPTY));
        }
        if (message.length() > MAX_LENGTH) {
            return Flux.just(ChatEvent.error(ChatErrorCode.TOO_LONG));
        }
        QuotaDecision decision = quota.tryConsume(clientKeyResolver.resolve(httpRequest), Instant.now());
        if (decision == QuotaDecision.MINUTE_LIMIT) {
            return Flux.just(ChatEvent.error(ChatErrorCode.MINUTE_LIMIT));
        }
        if (decision == QuotaDecision.DAILY_LIMIT) {
            return Flux.just(ChatEvent.error(ChatErrorCode.DAILY_LIMIT));
        }
        return chatService.answer(new ChatRequest(message, request.history(), request.caseSlug()));
    }

    private ServerSentEvent<String> toSse(ChatEvent event) {
        String data = event.data() instanceof String text ? text : mapper.writeValueAsString(event.data());
        return ServerSentEvent.builder(data).event(event.event()).build();
    }
}
