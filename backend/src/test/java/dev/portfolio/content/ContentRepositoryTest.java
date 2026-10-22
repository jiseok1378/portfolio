package dev.portfolio.content;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ContentRepositoryTest {

    private final ContentRepository repo = new ContentRepository(JsonMapper.builder().build());

    @Test
    void cases_areInIndexOrder() {
        assertThat(repo.cases()).extracting(CaseContent::slug).containsExactly(
                "realtime-records", "llm-tools-chatbot", "ad-card-designer", "parallel-testing", "auth-db-docker", "ai-harness");
    }

    @Test
    void everyCase_hasProblemAndSolutionAnd2to3Tags() {
        assertThat(repo.cases()).allSatisfy(c -> {
            assertThat(c.sections().problem()).isNotBlank();
            assertThat(c.sections().solution()).isNotEmpty();
            assertThat(c.tags()).hasSizeBetween(2, 3);
        });
    }

    @Test
    void findCase_unknownSlug_isEmpty() {
        assertThat(repo.findCase("nope")).isEmpty();
    }

    @Test
    void findCase_knownSlug_returnsIt() {
        assertThat(repo.findCase("parallel-testing")).map(CaseContent::title).hasValueSatisfying(t -> assertThat(t).contains("병렬"));
    }

    @Test
    void profile_blankFields_areEmptyStringsNotNull() {
        Profile p = repo.profile();
        assertThat(p.name()).isEmpty();
        assertThat(p.headline()).isEmpty();
        assertThat(p.email()).isEmpty();
        assertThat(p.phone()).isEmpty();
        assertThat(p.github()).isEmpty();
    }

    @Test
    void career_hasJobsAndSideProjects() {
        assertThat(repo.career().jobs()).isNotEmpty();
        assertThat(repo.career().sideProjects()).isNotEmpty();
    }
}
