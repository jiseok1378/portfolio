package dev.portfolio.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** SSE 답변이 서버 기본 비동기 제한(30초)에 먼저 끊기지 않도록, 서비스 제한(60초)보다 길게 둔다. */
@SpringBootTest(properties = "portfolio.vector-store-path=file:build/does-not-exist/vector-store.json")
class AsyncTimeoutConfigTest {

    @Autowired
    Environment env;

    @Test
    void asyncRequestTimeout_isLongerThanChatTimeout() {
        Duration timeout = env.getProperty("spring.mvc.async.request-timeout", Duration.class);
        assertThat(timeout).isNotNull();
        assertThat(timeout).isGreaterThan(Duration.ofSeconds(60));
    }
}
