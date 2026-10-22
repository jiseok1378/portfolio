package dev.portfolio.rag;

import dev.portfolio.content.ContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 커밋된 임베딩 파일이 지금 콘텐츠로 만든 것인지 확인한다. 콘텐츠를 고쳤다면 ./gradlew embed 를 다시 실행해야 한다. */
class VectorFreshnessTest {

    @Test
    void hash_isStableForSameDocuments() {
        var docs = List.of(new org.springframework.ai.document.Document("a", Map.of("slug", "x")));
        assertThat(ContentHash.of(docs)).isEqualTo(ContentHash.of(docs)).hasSize(64);
    }

    @Test
    void hash_changesWhenTextChanges() {
        var a = List.of(new org.springframework.ai.document.Document("a", Map.of("slug", "x")));
        var b = List.of(new org.springframework.ai.document.Document("b", Map.of("slug", "x")));
        assertThat(ContentHash.of(a)).isNotEqualTo(ContentHash.of(b));
    }

    @Test
    void committedVectorFile_matchesCurrentContent() throws Exception {
        var docs = new RagDocumentFactory(new ContentRepository(JsonMapper.builder().build())).create();
        var committed = new ClassPathResource("vector/content-hash.txt");
        assertThat(committed.exists()).as("vector/content-hash.txt 없음: ./gradlew embed 를 실행하세요").isTrue();
        assertThat(committed.getContentAsString(StandardCharsets.UTF_8).trim())
                .as("콘텐츠가 바뀌었습니다. ./gradlew embed 로 임베딩을 다시 만드세요")
                .isEqualTo(ContentHash.of(docs));
        assertThat(new ClassPathResource("vector/vector-store.json").exists()).isTrue();
    }
}
