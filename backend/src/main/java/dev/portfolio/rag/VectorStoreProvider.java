package dev.portfolio.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 빌드 때 미리 만든 벡터 파일을 시작 시 한 번 읽는다.
 * 파일이 없거나 깨져 있어도 서버는 떠야 하므로(사례 화면은 챗봇과 무관) 빈 값으로 둔다.
 */
@Component
public class VectorStoreProvider {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreProvider.class);

    private final VectorStore store;

    public VectorStoreProvider(EmbeddingModel embeddingModel,
                               @Value("${portfolio.vector-store-path}") Resource file) {
        this.store = load(embeddingModel, file);
    }

    public Optional<VectorStore> get() {
        return Optional.ofNullable(store);
    }

    private static VectorStore load(EmbeddingModel embeddingModel, Resource file) {
        if (!file.exists()) {
            log.warn("벡터 파일이 없어 챗봇을 사용할 수 없습니다: {}", file);
            return null;
        }
        try {
            SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
            store.load(file);
            return store;
        } catch (RuntimeException e) {
            log.warn("벡터 파일을 읽지 못해 챗봇을 사용할 수 없습니다: {}", file, e);
            return null;
        }
    }
}
