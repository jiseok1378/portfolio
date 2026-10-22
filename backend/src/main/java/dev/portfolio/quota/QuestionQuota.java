package dev.portfolio.quota;

import java.time.Instant;

/** 챗봇 질문 수 제한. 지금은 서버 한 대라 메모리 구현만 있고, 서버를 늘리면 Redis 구현으로 바꾼다. */
public interface QuestionQuota {

    /** 허용되면 한 번을 쓰고 ALLOWED, 아니면 거절 이유를 돌려준다. 거절은 카운트를 올리지 않는다. */
    QuotaDecision tryConsume(String clientKey, Instant now);
}
