package dev.portfolio.content;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** 공개 저장소에 올라가는 콘텐츠에 회사 내부 이름이나 허용되지 않은 링크가 섞이지 않았는지 검사한다. */
class ContentGuardTest {

    static final Path CONTENT = Path.of("..", "content");
    static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9._]+");
    static final Pattern URL = Pattern.compile("https?://[^\s\"]+");
    static final List<String> ALLOWED_URL_PREFIXES = List.of(
            "https://www.boannews.com/news/articleView.html?idxno=112964",
            "https://github.com/I-am-ok-developmnet/i-am-ok",
            "https://github.com/jiseok1378/claude-manual-kit",
            "https://github.com/jiseok1378/JsonCompare",
            "https://www.dbpia.co.kr/journal/articleDetail?nodeId=NODE09415023");

    @Test
    void contentHasNoForbiddenTerms() throws IOException {
        Set<String> forbidden = Files.readAllLines(CONTENT.resolve("forbidden-terms.sha256")).stream()
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        assertThat(forbidden).isNotEmpty();

        List<String> hits = new ArrayList<>();
        forEachText((where, text) -> {
            Matcher m = TOKEN.matcher(text);
            while (m.find()) {
                if (forbidden.contains(sha256(m.group().toLowerCase(Locale.ROOT)))) {
                    hits.add(where);
                }
            }
        });
        assertThat(hits).as("금칙어가 들어간 위치 (단어는 출력하지 않음)").isEmpty();
    }

    @Test
    void contentLinksAreAllowedOnly() throws IOException {
        List<String> bad = new ArrayList<>();
        forEachText((where, text) -> {
            Matcher m = URL.matcher(text);
            while (m.find()) {
                String url = m.group();
                if (ALLOWED_URL_PREFIXES.stream().noneMatch(url::startsWith)) {
                    bad.add(where + " -> " + url);
                }
            }
        });
        assertThat(bad).isEmpty();
    }

    interface TextVisitor {
        void visit(String where, String text);
    }

    static void forEachText(TextVisitor visitor) throws IOException {
        JsonMapper mapper = JsonMapper.builder().build();
        try (Stream<Path> files = Files.walk(CONTENT)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                JsonNode root = mapper.readTree(Files.readString(file, StandardCharsets.UTF_8));
                walk(file.getFileName() + ":$", root, visitor);
            }
        }
    }

    static void walk(String where, JsonNode node, TextVisitor visitor) {
        if (node.isString()) {
            visitor.visit(where, node.asString());
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                walk(where + "[" + i + "]", node.get(i), visitor);
            }
        } else if (node.isObject()) {
            for (var e : node.properties()) {
                walk(where + "." + e.getKey(), e.getValue(), visitor);
            }
        }
    }

    static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
