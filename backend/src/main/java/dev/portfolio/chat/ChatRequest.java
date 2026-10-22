package dev.portfolio.chat;

import java.util.List;

/** history 는 같은 탭 안의 최근 대화(서버에 저장하지 않음), caseSlug 는 사례 상세에서 물을 때만 온다. */
public record ChatRequest(String message, List<Turn> history, String caseSlug) {

    public ChatRequest {
        history = history == null ? List.of() : history;
    }

    public record Turn(String role, String content) {
    }
}
