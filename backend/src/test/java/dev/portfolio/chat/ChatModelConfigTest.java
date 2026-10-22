package dev.portfolio.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/** 기본 채팅 모델: 첫 글자까지 1초 안팎(gemini-3.8-flash 는 10초 넘게 걸려 제외). 환경 변수로 바꿀 수 있다. */
@SpringBootTest(properties = "portfolio.vector-store-path=file:build/does-not-exist/vector-store.json")
class ChatModelConfigTest {

    @Autowired
    Environment env;

    @Test
    void defaultChatModel_isGemini35FlashLite() {
        assertThat(env.getProperty("spring.ai.google.genai.chat.model")).isEqualTo("gemini-3.5-flash-lite");
    }

    @Test
    void embeddingModel_staysGeminiEmbedding001_soCommittedVectorsStayValid() {
        assertThat(env.getProperty("spring.ai.google.genai.embedding.text.model")).isEqualTo("gemini-embedding-001");
    }
}
