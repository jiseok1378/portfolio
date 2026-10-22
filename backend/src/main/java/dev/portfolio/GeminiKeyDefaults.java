package dev.portfolio;

import java.util.Map;

/**
 * GEMINI_API_KEY 가 없거나 빈 문자열이면 Spring AI 가 Vertex 모드로 바뀌어 서버가 뜨지 않는다.
 * application.yml 의 기본값(${GEMINI_API_KEY:not-configured})은 빈 문자열에는 적용되지 않으므로,
 * 시작 전에 자리표시 값을 넣는다. 이 경우 사례 화면은 정상이고 챗봇만 "사용할 수 없음"이 된다.
 */
public final class GeminiKeyDefaults {

    static final String PLACEHOLDER = "not-configured";

    private GeminiKeyDefaults() {
    }

    public static Map<String, String> forEnv(Map<String, String> env) {
        String key = env.get("GEMINI_API_KEY");
        if (key != null && !key.isBlank()) {
            return Map.of();
        }
        return Map.of(
                "spring.ai.google.genai.api-key", PLACEHOLDER,
                "spring.ai.google.genai.embedding.api-key", PLACEHOLDER);
    }
}
