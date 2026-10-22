package dev.portfolio.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/** 새 Gemini 계정은 gemini-2.5-flash 를 쓸 수 없어(404) 기본 채팅 모델을 바꾼다. 환경 변수로 바꿀 수 있다. */
@SpringBootTest(properties = "portfolio.vector-store-path=file:build/does-not-exist/vector-store.json")
class ChatModelConfigTest {

    @Autowired
    Environment env;

    @Test
    void defaultChatModel_isGemini38Flash() {
        assertThat(env.getProperty("spring.ai.google.genai.chat.model")).isEqualTo("gemini-3.8-flash");
    }

    @Test
    void embeddingModel_staysGeminiEmbedding001_soCommittedVectorsStayValid() {
        assertThat(env.getProperty("spring.ai.google.genai.embedding.text.model")).isEqualTo("gemini-embedding-001");
    }
}
