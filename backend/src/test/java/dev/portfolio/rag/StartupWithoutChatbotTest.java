package dev.portfolio.rag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** API 키와 벡터 파일이 없어도 서버는 떠야 한다(사례 화면은 챗봇과 무관). */
@SpringBootTest(properties = {
        "spring.ai.google.genai.api-key=not-configured",
        "spring.ai.google.genai.embedding.api-key=not-configured",
        "portfolio.vector-store-path=file:build/does-not-exist/vector-store.json"
})
class StartupWithoutChatbotTest {

    @Autowired
    VectorStoreProvider provider;

    @Test
    void contextStarts_andVectorStoreIsEmpty() {
        assertThat(provider.get()).isEmpty();
    }
}
