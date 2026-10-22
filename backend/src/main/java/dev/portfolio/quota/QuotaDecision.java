package dev.portfolio.quota;

public enum QuotaDecision {
    ALLOWED,
    MINUTE_LIMIT,
    /** 접속자 하루 한도 또는 전체 하루 한도 */
    DAILY_LIMIT
}
