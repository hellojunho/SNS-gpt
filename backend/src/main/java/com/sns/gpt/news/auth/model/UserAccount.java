package com.sns.gpt.news.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String subscriptionPlan;

    @Column(nullable = false)
    private long tokenUsed;

    @Column(nullable = false)
    private long tokenLimit;

    protected UserAccount() {
    }

    public UserAccount(String email, AuthProvider provider, String subscriptionPlan, long tokenUsed, long tokenLimit) {
        this.email = email;
        this.provider = provider;
        this.subscriptionPlan = subscriptionPlan;
        this.tokenUsed = tokenUsed;
        this.tokenLimit = tokenLimit;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public long getTokenUsed() {
        return tokenUsed;
    }

    public long getTokenLimit() {
        return tokenLimit;
    }

    public void updateTokenUsage(long used, long limit) {
        this.tokenUsed = used;
        this.tokenLimit = limit;
    }

    public void updatePlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }
}
