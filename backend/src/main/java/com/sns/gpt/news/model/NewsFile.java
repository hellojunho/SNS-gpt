package com.sns.gpt.news.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "news_files")
public class NewsFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsSource source;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private ZonedDateTime fetchedAt;

    @Column(nullable = false)
    private String filePath;

    protected NewsFile() {
    }

    public NewsFile(NewsSource source, String title, String sourceUrl, ZonedDateTime fetchedAt, String filePath) {
        this.source = source;
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.fetchedAt = fetchedAt;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public NewsSource getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public ZonedDateTime getFetchedAt() {
        return fetchedAt;
    }

    public String getFilePath() {
        return filePath;
    }
}
