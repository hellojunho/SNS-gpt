package com.sns.gpt.news.controller;

import com.sns.gpt.news.model.NewsFile;
import com.sns.gpt.news.model.NewsSource;
import com.sns.gpt.news.repository.NewsFileRepository;
import com.sns.gpt.news.service.ChatService;
import com.sns.gpt.news.service.FileStorageService;
import com.sns.gpt.news.service.NewsIngestService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NewsController {
    private final NewsFileRepository newsFileRepository;
    private final FileStorageService fileStorageService;
    private final NewsIngestService newsIngestService;
    private final ChatService chatService;

    public NewsController(NewsFileRepository newsFileRepository,
                          FileStorageService fileStorageService,
                          NewsIngestService newsIngestService,
                          ChatService chatService) {
        this.newsFileRepository = newsFileRepository;
        this.fileStorageService = fileStorageService;
        this.newsIngestService = newsIngestService;
        this.chatService = chatService;
    }

    @GetMapping("/news")
    public List<NewsFileResponse> listNews(@RequestParam("source") Optional<NewsSource> source) {
        List<NewsFile> files = source
                .map(value -> newsFileRepository.findTop20BySourceOrderByFetchedAtDesc(value))
                .orElseGet(newsFileRepository::findTop50ByOrderByFetchedAtDesc);
        return files.stream().map(NewsFileResponse::from).collect(Collectors.toList());
    }

    @GetMapping(value = "/news/{id}/content", produces = MediaType.TEXT_PLAIN_VALUE)
    public String readContent(@PathVariable Long id) throws IOException {
        NewsFile file = newsFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("News file not found"));
        return fileStorageService.readContent(file.getFilePath());
    }

    @GetMapping("/scheduler")
    public String schedulerInfo() {
        return "cron=" + newsIngestService.getCron() + ", timezone=" + newsIngestService.getTimezone();
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.respond(request.message()));
    }
}
