package com.sns.gpt.news.controller;

import com.sns.gpt.news.model.NewsFile;
import com.sns.gpt.news.model.NewsSource;
import java.time.ZonedDateTime;

public record NewsFileResponse(Long id,
                               NewsSource source,
                               String title,
                               String sourceUrl,
                               ZonedDateTime fetchedAt,
                               String filePath) {
    public static NewsFileResponse from(NewsFile newsFile) {
        return new NewsFileResponse(
                newsFile.getId(),
                newsFile.getSource(),
                newsFile.getTitle(),
                newsFile.getSourceUrl(),
                newsFile.getFetchedAt(),
                newsFile.getFilePath());
    }
}
