package com.sns.gpt.news.service;

import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final SecretsService secretsService;

    public ChatService(SecretsService secretsService) {
        this.secretsService = secretsService;
    }

    public String respond(String message) {
        String apiKeyHint = secretsService.getSecret("openaiApiKey") != null ? "configured" : "missing";
        return "[경제/정치 전문가 ChatGPT] 요청하신 질문에 대해 요약해 답변드리겠습니다: "
                + message
                + " (" + ZonedDateTime.now() + ", apiKey=" + apiKeyHint + ")";
    }
}
