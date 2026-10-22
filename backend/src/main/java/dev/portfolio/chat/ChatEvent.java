package dev.portfolio.chat;

import java.util.List;
import java.util.Map;

/**
 * 챗봇 SSE 이벤트. 순서: token* → sources → done. 오류면 그 자리에서 error 하나로 끝난다.
 * token 의 data 는 글자 조각(문자열), 나머지는 JSON 으로 보낸다.
 */
public record ChatEvent(String event, Object data) {

    public static ChatEvent token(String text) {
        return new ChatEvent("token", text);
    }

    public static ChatEvent sources(List<Map<String, String>> sources) {
        return new ChatEvent("sources", sources);
    }

    public static ChatEvent done() {
        return new ChatEvent("done", Map.of());
    }

    public static ChatEvent error(ChatErrorCode code) {
        return new ChatEvent("error", Map.of("code", code.name(), "message", code.message()));
    }
}
