package dev.portfolio.content;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

/** classpath의 content/ JSON(저장소 루트 content/를 빌드 때 복사)을 시작 시 한 번 읽어 보관한다. */
@Component
public class ContentRepository {

    private final List<CaseContent> cases;
    private final CareerContent career;
    private final Profile profile;

    public ContentRepository(ObjectMapper mapper) {
        List<String> order = read(mapper, "content/cases/index.json", new TypeReference<>() { });
        this.cases = order.stream()
                .map(slug -> read(mapper, "content/cases/" + slug + ".json", new TypeReference<CaseContent>() { }))
                .toList();
        this.career = read(mapper, "content/career.json", new TypeReference<>() { });
        this.profile = read(mapper, "content/profile.json", new TypeReference<>() { });
    }

    public List<CaseContent> cases() {
        return cases;
    }

    public Optional<CaseContent> findCase(String slug) {
        return cases.stream().filter(c -> c.slug().equals(slug)).findFirst();
    }

    public CareerContent career() {
        return career;
    }

    public Profile profile() {
        return profile;
    }

    private static <T> T read(ObjectMapper mapper, String path, TypeReference<T> type) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return mapper.readValue(in, type);
        } catch (IOException e) {
            throw new UncheckedIOException("콘텐츠 파일을 읽지 못했습니다: " + path, e);
        }
    }
}
