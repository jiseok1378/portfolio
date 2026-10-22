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
import java.util.stream.Stream;

/** classpath의 content/ JSON(저장소 루트 content/를 빌드 때 복사)을 시작 시 한 번 읽어 보관한다. */
@Component
public class ContentRepository {

    private final List<CaseContent> cases;
    private final CareerContent career;
    private final Profile profile;

    public ContentRepository(ObjectMapper mapper) {
        CaseOrder order = read(mapper, "content/cases/index.json", new TypeReference<>() { });
        this.cases = Stream.concat(order.featured().stream(), order.others().stream())
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

    /** 대표 사례와 그 밖의 사례. 챗봇은 둘을 구분하지 않고 모두 쓴다. */
    private record CaseOrder(List<String> featured, List<String> others) {
    }

    private static <T> T read(ObjectMapper mapper, String path, TypeReference<T> type) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return mapper.readValue(in, type);
        } catch (IOException e) {
            throw new UncheckedIOException("콘텐츠 파일을 읽지 못했습니다: " + path, e);
        }
    }
}
