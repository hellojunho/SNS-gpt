package com.sns.gpt.news.service;

import com.sns.gpt.news.model.NewsFile;
import com.sns.gpt.news.model.NewsSource;
import com.sns.gpt.news.repository.NewsFileRepository;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsIngestService {
    private final BloombergFetcher bloombergFetcher;
    private final InvestingFetcher investingFetcher;
    private final FileStorageService fileStorageService;
    private final NewsFileRepository newsFileRepository;
    private final GptSummaryService gptSummaryService;
    private final String cron;
    private final String timezone;

    public NewsIngestService(BloombergFetcher bloombergFetcher,
                             InvestingFetcher investingFetcher,
                             FileStorageService fileStorageService,
                             NewsFileRepository newsFileRepository,
                             GptSummaryService gptSummaryService,
                             @Value("${app.scheduler.cron}") String cron,
                             @Value("${app.scheduler.timezone}") String timezone) {
        this.bloombergFetcher = bloombergFetcher;
        this.investingFetcher = investingFetcher;
        this.fileStorageService = fileStorageService;
        this.newsFileRepository = newsFileRepository;
        this.gptSummaryService = gptSummaryService;
        this.cron = cron;
        this.timezone = timezone;
    }

    @Scheduled(cron = "${app.scheduler.cron}", zone = "${app.scheduler.timezone}")
    public void scheduledIngest() {
        ingestAllSources();
    }

    @Transactional
    public void ingestAllSources() {
        ingestSource(NewsSource.BLOOMBERG, () -> bloombergFetcher.fetchTopNews());
        ingestSource(NewsSource.INVESTING_USA, () -> investingFetcher.fetchTopNews("https://www.investing.com"));
        ingestSource(NewsSource.INVESTING_KOREA, () -> investingFetcher.fetchTopNews("https://kr.investing.com"));
        ingestSource(NewsSource.INVESTING_JAPAN, () -> investingFetcher.fetchTopNews("https://jp.investing.com"));
        ingestSource(NewsSource.INVESTING_CHINA, () -> investingFetcher.fetchTopNews("https://cn.investing.com"));
        generateGpt10Summary();
    }

    private void ingestSource(NewsSource source, Fetcher fetcher) {
        try {
            List<NewsItem> items = fetcher.fetch();
            for (NewsItem item : items) {
                saveItem(source, item);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to ingest source: " + source, ex);
        }
    }

    private void saveItem(NewsSource source, NewsItem item) throws IOException {
        ZonedDateTime fetchedAt = item.fetchedAt();
        String filePath = fileStorageService.writeContent(
                source.name(),
                item.title(),
                item.url(),
                fetchedAt,
                item.content());
        newsFileRepository.save(new NewsFile(source, item.title(), item.url(), fetchedAt, filePath));
    }

    private void generateGpt10Summary() {
        try {
            NewsItem summary = gptSummaryService.generateSummary();
            saveItem(NewsSource.GPT10, summary);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate GPT10 summary", ex);
        }
    }

    public String getCron() {
        return cron;
    }

    public String getTimezone() {
        return timezone;
    }

    @FunctionalInterface
    interface Fetcher {
        List<NewsItem> fetch() throws IOException;
    }
}
