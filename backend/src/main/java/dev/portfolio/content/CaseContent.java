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
}
