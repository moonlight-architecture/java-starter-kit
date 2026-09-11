package com.servicecops.project.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Prod (and uat) refuse to start with the committed dummy JWT secret.
 */
@Component
@Profile({"prod", "uat"})
public class JwtSecretGuard {

    static final String DUMMY_SECRET = "Q0hBTkdFX01FX0NIQU5HRV9NRV9DSEFOR0VfTUVfMzJCIQ==";

    @Value("${secret}")
    private String secret;

    @PostConstruct
    void rejectDummySecret() {
        if (DUMMY_SECRET.equals(secret)) {
            throw new IllegalStateException("Set JWT_SECRET before running this profile");
        }
    }
}
