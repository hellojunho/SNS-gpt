package com.sns.gpt.news.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretsService {
    private final Path secretsPath;
    private final ObjectMapper objectMapper;
    private Map<String, String> cache;

    public SecretsService(@Value("${app.secrets.path}") String secretsPath, ObjectMapper objectMapper) {
        this.secretsPath = Path.of(secretsPath);
        this.objectMapper = objectMapper;
    }

    public String getSecret(String key) {
        return loadSecrets().get(key);
    }

    private Map<String, String> loadSecrets() {
        if (cache != null) {
            return cache;
        }
        if (!Files.exists(secretsPath)) {
            cache = Collections.emptyMap();
            return cache;
        }
        try {
            cache = objectMapper.readValue(
                    Files.readString(secretsPath),
                    new TypeReference<Map<String, String>>() {
                    });
            return cache;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read secrets.json", ex);
        }
    }
}
