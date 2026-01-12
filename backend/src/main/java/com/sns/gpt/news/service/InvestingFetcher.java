package com.sns.gpt.news.service;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class InvestingFetcher {
    private static final String INVESTING_NEWS_URL = "https://www.investing.com/news/stock-market-news";

    public List<NewsItem> fetchTopNews() throws IOException {
        Document document = Jsoup.connect(INVESTING_NEWS_URL)
                .userAgent("Mozilla/5.0")
                .get();

        List<Element> headlineLinks = document.select("article a[href*=/news/]");
        List<String> urls = headlineLinks.stream()
                .map(link -> link.absUrl("href"))
                .distinct()
                .limit(8)
                .collect(Collectors.toList());

        List<NewsItem> items = new ArrayList<>();
        for (String url : urls) {
            items.add(fetchArticle(url));
        }
        return items;
    }

    private NewsItem fetchArticle(String url) throws IOException {
        Document articleDocument = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();
        String title = articleDocument.title();
        String content = extractArticleContent(articleDocument);
        ZonedDateTime fetchedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return new NewsItem(title, url, content, fetchedAt);
    }

    private String extractArticleContent(Document document) {
        Element article = document.selectFirst("div.articlePage");
        if (article != null) {
            String text = article.text();
            if (!text.isBlank()) {
                return text;
            }
        }
        Element content = document.selectFirst("div.article-content");
        if (content != null) {
            return content.text();
        }
        return document.text();
    }
}
