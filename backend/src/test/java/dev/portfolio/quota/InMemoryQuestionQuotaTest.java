package dev.portfolio.quota;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryQuestionQuotaTest {

    private static final Instant T0 = Instant.parse("2026-10-22T03:00:00Z"); // 서울 12:00

    private InMemoryQuestionQuota quota(int perMinute, int perDay, int globalPerDay) {
        return new InMemoryQuestionQuota(perMinute, perDay, globalPerDay);
    }

    @Test
    void sixthQuestionInSameMinute_isMinuteLimit() {
        var q = quota(5, 30, 200);
        for (int i = 0; i < 5; i++) {
            assertThat(q.tryConsume("a", T0.plusSeconds(i))).isEqualTo(QuotaDecision.ALLOWED);
        }
        assertThat(q.tryConsume("a", T0.plusSeconds(10))).isEqualTo(QuotaDecision.MINUTE_LIMIT);
    }

    @Test
    void afterOneMinute_allowedAgain() {
        var q = quota(5, 30, 200);
        for (int i = 0; i < 5; i++) {
            q.tryConsume("a", T0);
        }
        assertThat(q.tryConsume("a", T0.plusSeconds(61))).isEqualTo(QuotaDecision.ALLOWED);
    }

    @Test
    void thirtyFirstQuestionInDay_isDailyLimit() {
        var q = quota(5, 30, 200);
        for (int i = 0; i < 30; i++) {
            assertThat(q.tryConsume("a", T0.plusSeconds(i * 61L))).isEqualTo(QuotaDecision.ALLOWED);
        }
        assertThat(q.tryConsume("a", T0.plusSeconds(31 * 61L))).isEqualTo(QuotaDecision.DAILY_LIMIT);
    }

    @Test
    void dailyCountResetsAtSeoulMidnight() {
        var q = quota(5, 1, 200);
        Instant beforeMidnight = Instant.parse("2026-10-22T14:59:00Z"); // 서울 23:59
        assertThat(q.tryConsume("a", beforeMidnight)).isEqualTo(QuotaDecision.ALLOWED);
        assertThat(q.tryConsume("a", beforeMidnight.plusSeconds(30))).isEqualTo(QuotaDecision.DAILY_LIMIT);
        assertThat(q.tryConsume("a", Instant.parse("2026-10-22T15:00:00Z"))).isEqualTo(QuotaDecision.ALLOWED);
    }

    @Test
    void globalDailyLimit_holdsAcrossManyClientKeys() {
        var q = quota(5, 30, 200);
        for (int i = 0; i < 200; i++) {
            assertThat(q.tryConsume("ip-" + i, T0)).isEqualTo(QuotaDecision.ALLOWED);
        }
        assertThat(q.tryConsume("ip-new", T0)).isEqualTo(QuotaDecision.DAILY_LIMIT);
    }

    @Test
    void rejectedRequest_doesNotConsume() {
        var q = quota(1, 2, 200);
        assertThat(q.tryConsume("a", T0)).isEqualTo(QuotaDecision.ALLOWED);
        assertThat(q.tryConsume("a", T0.plusSeconds(1))).isEqualTo(QuotaDecision.MINUTE_LIMIT);
        assertThat(q.tryConsume("a", T0.plusSeconds(2))).isEqualTo(QuotaDecision.MINUTE_LIMIT);
        // 거절된 요청이 하루 카운트를 올렸다면 여기서 DAILY_LIMIT 이 된다
        assertThat(q.tryConsume("a", T0.plusSeconds(70))).isEqualTo(QuotaDecision.ALLOWED);
    }

    @Test
    void concurrentConsume_neverExceedsGlobalLimit() throws Exception {
        var q = quota(1000, 1000, 200);
        AtomicInteger allowed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(16);
        for (int t = 0; t < 16; t++) {
            int thread = t;
            pool.submit(() -> {
                for (int i = 0; i < 50; i++) {
                    if (q.tryConsume("ip-" + thread + "-" + (i % 5), T0) == QuotaDecision.ALLOWED) {
                        allowed.incrementAndGet();
                    }
                }
            });
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        assertThat(allowed.get()).isEqualTo(200);
    }
}
