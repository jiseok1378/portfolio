package dev.portfolio.content;

import java.util.List;

public record CareerContent(
        List<Job> jobs,
        List<SideProject> sideProjects,
        List<Education> education,
        List<Certification> certifications) {

    public record Job(String period, String company, String role, List<String> stack, List<Story> stories) {
    }

    public record Story(String title, String body, List<Link> links) {
    }

    public record SideProject(String name, String period, String body, List<Link> links) {
    }

    public record Education(String period, String name) {
    }

    public record Certification(String date, String name) {
    }
}
