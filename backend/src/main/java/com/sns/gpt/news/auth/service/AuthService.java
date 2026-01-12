package com.sns.gpt.news.auth.service;

import com.sns.gpt.news.auth.model.AuthProvider;
import com.sns.gpt.news.auth.model.UserAccount;
import com.sns.gpt.news.auth.model.UserSession;
import com.sns.gpt.news.auth.repository.UserAccountRepository;
import com.sns.gpt.news.auth.repository.UserSessionRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final long DEFAULT_TOKEN_LIMIT = 100000;
    private static final String DEFAULT_PLAN = "ChatGPT Plus";

    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;

    public AuthService(UserAccountRepository userAccountRepository, UserSessionRepository userSessionRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public UserSession loginWithGoogle(String email) {
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseGet(() -> userAccountRepository.save(
                        new UserAccount(email, AuthProvider.CHATGPT_GOOGLE, DEFAULT_PLAN, 0, DEFAULT_TOKEN_LIMIT)));
        userSessionRepository.deleteByExpiresAtBefore(ZonedDateTime.now(SEOUL));
        String token = UUID.randomUUID().toString();
        ZonedDateTime expiresAt = ZonedDateTime.now(SEOUL).plusDays(1);
        return userSessionRepository.save(new UserSession(user, token, expiresAt));
    }

    public Optional<UserSession> findSession(String sessionToken) {
        return userSessionRepository.findBySessionToken(sessionToken)
                .filter(session -> session.getExpiresAt().isAfter(ZonedDateTime.now(SEOUL)));
    }

    @Transactional
    public void logout(String sessionToken) {
        userSessionRepository.findBySessionToken(sessionToken)
                .ifPresent(userSessionRepository::delete);
    }
}
