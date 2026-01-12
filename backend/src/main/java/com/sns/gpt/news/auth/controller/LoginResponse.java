package com.sns.gpt.news.auth.controller;

import java.time.ZonedDateTime;

public record LoginResponse(String sessionToken, ZonedDateTime expiresAt) {
}
