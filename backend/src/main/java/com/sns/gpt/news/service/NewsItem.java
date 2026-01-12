package com.sns.gpt.news.service;

import java.time.ZonedDateTime;

public record NewsItem(String title, String url, String content, ZonedDateTime fetchedAt) {
}
