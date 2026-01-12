package com.sns.gpt.news.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(nullable = false, unique = true)
    private String sessionToken;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;

    protected UserSession() {
    }

    public UserSession(UserAccount user, String sessionToken, ZonedDateTime expiresAt) {
        this.user = user;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }
}
