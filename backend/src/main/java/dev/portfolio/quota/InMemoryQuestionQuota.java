package dev.portfolio.quota;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * 서버 메모리에 질문 수를 센다. 하루 경계는 서울 자정.
 * 요청이 적은 사이트라 전체를 한 잠금으로 묶어 전체 한도를 정확히 지킨다.
 * 서버가 재시작되거나 잠들면 카운트가 초기화된다(최종 방어선은 Gemini 쪽 한도).
 */
@Component
public class InMemoryQuestionQuota implements QuestionQuota {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Duration MINUTE = Duration.ofMinutes(1);

    private final int perMinute;
    private final int perDay;
    private final int globalPerDay;

    private final Map<String, ClientUsage> clients = new HashMap<>();
    private LocalDate day;
    private int globalCount;

    public InMemoryQuestionQuota(@Value("${portfolio.quota.per-minute:5}") int perMinute,
                                 @Value("${portfolio.quota.per-day:30}") int perDay,
                                 @Value("${portfolio.quota.global-per-day:200}") int globalPerDay) {
        this.perMinute = perMinute;
        this.perDay = perDay;
        this.globalPerDay = globalPerDay;
    }

    @Override
    public synchronized QuotaDecision tryConsume(String clientKey, Instant now) {
        LocalDate today = LocalDate.ofInstant(now, SEOUL);
        if (!today.equals(day)) {
            day = today;
            globalCount = 0;
            clients.clear();
        }

        ClientUsage usage = clients.computeIfAbsent(clientKey, k -> new ClientUsage());
        Instant windowStart = now.minus(MINUTE);
        while (!usage.recent.isEmpty() && !usage.recent.peekFirst().isAfter(windowStart)) {
            usage.recent.pollFirst();
        }

        if (usage.dayCount >= perDay || globalCount >= globalPerDay) {
            return QuotaDecision.DAILY_LIMIT;
        }
        if (usage.recent.size() >= perMinute) {
            return QuotaDecision.MINUTE_LIMIT;
        }
        usage.recent.addLast(now);
        usage.dayCount++;
        globalCount++;
        return QuotaDecision.ALLOWED;
    }

    private static final class ClientUsage {
        private final Deque<Instant> recent = new ArrayDeque<>();
        private int dayCount;
    }
}
