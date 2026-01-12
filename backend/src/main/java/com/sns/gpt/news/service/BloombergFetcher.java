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
public class BloombergFetcher {
    private static final String BLOOMBERG_MARKETS_URL = "https://www.bloomberg.com/markets";

    public List<NewsItem> fetchTopNews() throws IOException {
        Document document = Jsoup.connect(BLOOMBERG_MARKETS_URL)
                .userAgent("Mozilla/5.0")
                .get();

        List<Element> headlineLinks = document.select("a[href*=/news/]");
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
        Element article = document.selectFirst("article");
        if (article != null) {
            String text = article.text();
            if (!text.isBlank()) {
                return text;
            }
        }
        Element body = document.selectFirst("div.body-content");
        if (body != null) {
            return body.text();
        }
        return document.text();
    }
}
