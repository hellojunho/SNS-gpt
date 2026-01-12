package com.sns.gpt.news.auth.controller;

import com.sns.gpt.news.auth.model.AuthProvider;
import com.sns.gpt.news.auth.model.UserAccount;

public record UserProfileResponse(String email,
                                  AuthProvider provider,
                                  String subscriptionPlan,
                                  long tokenUsed,
                                  long tokenLimit) {
    public static UserProfileResponse from(UserAccount account) {
        return new UserProfileResponse(
                account.getEmail(),
                account.getProvider(),
                account.getSubscriptionPlan(),
                account.getTokenUsed(),
                account.getTokenLimit());
    }
}
