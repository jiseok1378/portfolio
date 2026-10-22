package dev.portfolio.chat;

public enum ChatErrorCode {
    TOO_LONG("질문은 500자 이내로 입력해 주세요."),
    EMPTY("질문을 입력해 주세요."),
    MINUTE_LIMIT("질문이 너무 빠릅니다. 잠시 후 다시 시도해 주세요."),
    DAILY_LIMIT("오늘 질문 한도가 끝났습니다. 내일 다시 질문해 주세요."),
    UNAVAILABLE("챗봇을 잠시 사용할 수 없습니다. 사례 글은 그대로 보실 수 있습니다.");

    private final String message;

    ChatErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
