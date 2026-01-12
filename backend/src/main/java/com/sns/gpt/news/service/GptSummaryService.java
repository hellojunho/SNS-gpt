package com.sns.gpt.news.service;

import com.sns.gpt.news.model.NewsFile;
import com.sns.gpt.news.repository.NewsFileRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GptSummaryService {
    private final NewsFileRepository newsFileRepository;

    public GptSummaryService(NewsFileRepository newsFileRepository) {
        this.newsFileRepository = newsFileRepository;
    }

    public NewsItem generateSummary() {
        List<NewsFile> latest = newsFileRepository.findTop50ByOrderByFetchedAtDesc();
        String content = buildSummary(latest);
        ZonedDateTime fetchedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return new NewsItem("GPT10 Daily Macro Summary", "local://gpt10", content, fetchedAt);
    }

    private String buildSummary(List<NewsFile> files) {
        String header = "Top 10 Market-Moving Headlines\n";
        List<String> topTen = files.stream()
                .limit(10)
                .map(file -> "- [" + file.getSource() + "] " + file.getTitle() + " (" + file.getSourceUrl() + ")")
                .collect(Collectors.toList());
        if (topTen.isEmpty()) {
            return header + "No headlines available yet.";
        }
        return header + String.join("\n", topTen);
    }
}
