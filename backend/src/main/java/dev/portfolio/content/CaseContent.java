package dev.portfolio.content;

import java.util.List;

public record CaseContent(
        String slug,
        String title,
        String period,
        String context,
        String summary,
        String metric,
        List<String> tags,
        boolean unfinished,
        String diagram,
        Sections sections,
        List<Snippet> snippets,
        List<Screenshot> screenshots,
        List<Link> links) {

    public record Sections(
            String problem,
            List<String> thinking,
            List<String> solution,
            List<String> result,
            List<String> lessons,
            List<Qa> interviewQuestions) {
    }

    public record Qa(String q, String a) {
    }

    /** 사례 화면에만 보여주는 코드. 없으면 null 이고, RAG 문서에는 넣지 않는다. */
    public record Snippet(String title, String lang, String code) {
    }

    /** 사례 화면에만 보여주는 스크린샷(frontend/public 기준 경로). 없으면 null 이고, RAG 문서에는 넣지 않는다. */
    public record Screenshot(String src, String caption) {
    }
}
