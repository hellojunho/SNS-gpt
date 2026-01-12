package com.sns.gpt.news.auth.repository;

import com.sns.gpt.news.auth.model.UserSession;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findBySessionToken(String sessionToken);
    long deleteByExpiresAtBefore(ZonedDateTime cutoff);
}
