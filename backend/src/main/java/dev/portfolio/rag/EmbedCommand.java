package dev.portfolio.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/** `--embed=<출력 파일>` 인자로 실행하면 콘텐츠를 임베딩해 파일로 저장하고 종료한다 (Gradle `embed` 태스크). */
@Component
public class EmbedCommand implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmbedCommand.class);

    private final RagDocumentFactory documents;
    private final EmbeddingModel embeddingModel;
    private final ConfigurableApplicationContext context;

    public EmbedCommand(RagDocumentFactory documents, EmbeddingModel embeddingModel,
                        ConfigurableApplicationContext context) {
        this.documents = documents;
        this.embeddingModel = embeddingModel;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("embed")) {
            return;
        }
        File out = new File(args.getOptionValues("embed").get(0));
        List<Document> docs = documents.create();
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        store.add(docs);
        if (out.getParentFile() != null) {
            out.getParentFile().mkdirs();
        }
        store.save(out);
        // 어떤 콘텐츠로 만든 파일인지 남겨, 콘텐츠만 바뀌고 임베딩을 빠뜨리면 테스트가 잡게 한다
        try {
            java.nio.file.Files.writeString(new File(out.getParentFile(), "content-hash.txt").toPath(),
                    ContentHash.of(docs) + "\n");
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        log.info("임베딩 완료: 문서 {}개 -> {}", docs.size(), out.getAbsolutePath());
        System.exit(SpringApplication.exit(context, () -> 0));
    }
}
