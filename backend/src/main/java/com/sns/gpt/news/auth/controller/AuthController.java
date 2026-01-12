package com.sns.gpt.news.auth.controller;

import com.sns.gpt.news.auth.model.UserSession;
import com.sns.gpt.news.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserSession session = authService.loginWithGoogle(request.email());
        return new LoginResponse(session.getSessionToken(), session.getExpiresAt());
    }

    @GetMapping("/me")
    public UserProfileResponse me(@RequestHeader("Authorization") Optional<String> authorization) {
        String token = extractToken(authorization);
        UserSession session = authService.findSession(token)
                .orElseThrow(() -> new UnauthorizedException("Session expired"));
        return UserProfileResponse.from(session.getUser());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") Optional<String> authorization) {
        String token = extractToken(authorization);
        authService.logout(token);
    }

    private String extractToken(Optional<String> authorization) {
        String value = authorization.orElse("");
        if (value.startsWith("Bearer ")) {
            return value.substring(7);
        }
        throw new UnauthorizedException("Missing authorization token");
    }
}
