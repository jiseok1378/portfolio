package dev.portfolio;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiKeyDefaultsTest {

    @Test
    void blankKey_getsPlaceholder_soServerStillStarts() {
        assertThat(GeminiKeyDefaults.forEnv(Map.of("GEMINI_API_KEY", "  ")))
                .containsEntry("spring.ai.google.genai.api-key", "not-configured")
                .containsEntry("spring.ai.google.genai.embedding.api-key", "not-configured");
    }

    @Test
    void missingKey_getsPlaceholder() {
        assertThat(GeminiKeyDefaults.forEnv(Map.of())).containsEntry("spring.ai.google.genai.api-key", "not-configured");
    }

    @Test
    void realKey_isLeftAlone() {
        assertThat(GeminiKeyDefaults.forEnv(Map.of("GEMINI_API_KEY", "AIzaXXXX"))).isEmpty();
    }
}
