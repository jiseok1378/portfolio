package dev.portfolio.rag;

import dev.portfolio.content.CareerContent;
import dev.portfolio.content.CaseContent;
import dev.portfolio.content.ContentRepository;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 콘텐츠를 RAG 검색 단위(사례 섹션 하나, 경력 이야기 하나)의 문서로 나눈다. */
@Component
public class RagDocumentFactory {

    public static final String CAREER_SLUG = "career";

    private final ContentRepository content;

    public RagDocumentFactory(ContentRepository content) {
        this.content = content;
    }

    public List<Document> create() {
        List<Document> docs = new ArrayList<>();
        for (CaseContent c : content.cases()) {
            addCaseDocs(docs, c);
        }
        addCareerDocs(docs, content.career());
        return docs;
    }

    private void addCaseDocs(List<Document> docs, CaseContent c) {
        CaseContent.Sections s = c.sections();
        String overview = c.context() + " · " + c.period() + "\n" + c.summary()
                + (c.metric() == null ? "" : "\n핵심 수치: " + c.metric())
                + (c.unfinished() ? "\n미완성 사이드 프로젝트입니다." : "")
                + "\n기술: " + String.join(", ", c.tags());
        addCase(docs, c, "개요", overview);
        addCase(docs, c, "문제", s.problem());
        addCase(docs, c, "고민", lines(s.thinking()));
        addCase(docs, c, "해결", lines(s.solution()));
        addCase(docs, c, "결과", lines(s.result()));
        addCase(docs, c, "배운 점", lines(s.lessons()));
        addCase(docs, c, "면접 예상 질문", s.interviewQuestions().stream()
                .map(qa -> "Q. " + qa.q() + "\nA. " + qa.a())
                .collect(Collectors.joining("\n")));
    }

    private void addCase(List<Document> docs, CaseContent c, String section, String body) {
        if (body == null || body.isBlank()) {
            return;
        }
        docs.add(new Document("[" + c.title() + "] " + section + "\n" + body,
                Map.of("slug", c.slug(), "title", c.title())));
    }

    private void addCareerDocs(List<Document> docs, CareerContent career) {
        String summary = career.jobs().stream()
                .map(j -> j.period() + " " + j.company() + " (" + j.role() + ") — " + String.join(", ", j.stack()))
                .collect(Collectors.joining("\n"));
        addCareer(docs, "경력 요약", summary);
        for (CareerContent.Job job : career.jobs()) {
            for (CareerContent.Story story : job.stories()) {
                addCareer(docs, job.company() + " — " + story.title(), "기간: " + job.period() + "\n" + story.body());
            }
        }
        for (CareerContent.SideProject p : career.sideProjects()) {
            addCareer(docs, "사이드 프로젝트 — " + p.name(),
                    (p.period() == null ? "" : "기간: " + p.period() + "\n") + p.body());
        }
        String education = career.education().stream().map(e -> e.period() + " " + e.name())
                .collect(Collectors.joining("\n"));
        String certs = career.certifications().stream().map(e -> e.date() + " " + e.name())
                .collect(Collectors.joining("\n"));
        addCareer(docs, "학력·자격·교육", education + "\n" + certs);
    }

    private void addCareer(List<Document> docs, String title, String body) {
        docs.add(new Document("[" + title + "]\n" + body, Map.of("slug", CAREER_SLUG, "title", title)));
    }

    private static String lines(List<String> items) {
        return items.stream().map(s -> "- " + s).collect(Collectors.joining("\n"));
    }
}
