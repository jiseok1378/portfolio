package dev.portfolio.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VectorStoreProviderTest {

    private final FakeEmbeddingModel embedding = new FakeEmbeddingModel();

    @Test
    void missingFile_returnsEmpty_andDoesNotThrow(@TempDir Path dir) {
        var provider = new VectorStoreProvider(embedding, new FileSystemResource(dir.resolve("none.json")));
        assertThat(provider.get()).isEmpty();
    }

    @Test
    void corruptFile_returnsEmpty_andDoesNotThrow(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("broken.json");
        Files.writeString(file, "{not json");
        var provider = new VectorStoreProvider(embedding, new FileSystemResource(file));
        assertThat(provider.get()).isEmpty();
    }

    @Test
    void validFile_returnsSearchableStore(@TempDir Path dir) {
        Path file = dir.resolve("store.json");
        SimpleVectorStore store = SimpleVectorStore.builder(embedding).build();
        store.add(List.of(
                new Document("Kafka DLQ 설계", Map.of("slug", "realtime-records", "title", "실시간")),
                new Document("병렬 처리", Map.of("slug", "parallel-testing", "title", "병렬"))));
        store.save(file.toFile());

        var provider = new VectorStoreProvider(embedding, new FileSystemResource(file));

        assertThat(provider.get()).isPresent();
        assertThat(provider.get().orElseThrow().similaritySearch(SearchRequest.builder().query("Kafka").topK(2).build()))
                .hasSize(2);
    }
}
