package com.sns.gpt.news.controller;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String message) {
}
