package com.sns.gpt.news.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {
    private final Path basePath;

    public FileStorageService(@Value("${app.storage.base-path}") String basePath) {
        this.basePath = Paths.get(basePath);
    }

    public String writeContent(String source, String title, String url, ZonedDateTime fetchedAt, String content)
            throws IOException {
        Files.createDirectories(basePath);
        String timestamp = fetchedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String safeTitle = title.replaceAll("[^a-zA-Z0-9가-힣_-]", "_");
        String fileName = String.format("%s_%s_%s_%s.txt", source, timestamp, safeTitle, UUID.randomUUID());
        Path filePath = basePath.resolve(fileName);
        String payload = "Source: " + source + System.lineSeparator()
                + "FetchedAt: " + fetchedAt + System.lineSeparator()
                + "URL: " + url + System.lineSeparator()
                + System.lineSeparator()
                + content;
        Files.writeString(filePath, payload, StandardCharsets.UTF_8);
        return filePath.toAbsolutePath().toString();
    }

    public String readContent(String absolutePath) throws IOException {
        return Files.readString(Path.of(absolutePath), StandardCharsets.UTF_8);
    }
}
