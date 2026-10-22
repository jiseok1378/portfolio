package dev.portfolio.rag;

import dev.portfolio.content.CaseContent;
import dev.portfolio.content.ContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagDocumentFactoryTest {

    private final ContentRepository repo = new ContentRepository(JsonMapper.builder().build());
    private final RagDocumentFactory factory = new RagDocumentFactory(repo);

    @Test
    void create_makesDocsForEveryCase_withSlugAndTitleMetadata() {
        List<Document> docs = factory.create();
        for (CaseContent c : repo.cases()) {
            assertThat(docs).anySatisfy(d -> {
                assertThat(d.getMetadata()).containsEntry("slug", c.slug());
                assertThat(d.getMetadata()).containsEntry("title", c.title());
            });
        }
        assertThat(docs).allSatisfy(d -> assertThat(d.getText()).isNotBlank());
    }

    @Test
    void create_caseDocTextStartsWithTitleAndSectionName() {
        List<Document> docs = factory.create();
        assertThat(docs).filteredOn(d -> "realtime-records".equals(d.getMetadata().get("slug")))
                .extracting(d -> d.getText().lines().findFirst().orElse(""))
                .contains("[실시간 경기 기록 파이프라인] 문제", "[실시간 경기 기록 파이프라인] 해결");
    }

    @Test
    void create_careerStoriesAndSideProjects_useCareerSlug() {
        List<Document> docs = factory.create();
        assertThat(docs).anySatisfy(d -> {
            assertThat(d.getMetadata()).containsEntry("slug", RagDocumentFactory.CAREER_SLUG);
            assertThat(d.getText()).contains("WebSocket");
        });
        assertThat(docs).anySatisfy(d -> assertThat(d.getText()).contains("CI/CD"));
    }

    @Test
    void create_skipsEmptySections() {
        // ai-harness 사례는 면접 예상 질문이 비어 있다
        assertThat(factory.create()).filteredOn(d -> "ai-harness".equals(d.getMetadata().get("slug")))
                .noneSatisfy(d -> assertThat(d.getText()).startsWith("[AI 작업 환경 설계 — 같은 실수를 두 번 하지 않게] 면접 예상 질문"));
    }
}
