package com.mongodb.kitchensink.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(value = "app", ignoreUnknownFields = false)
public record AppConfigProperties(List<String> defaultUserRoles, List<String> allowedOrigins, JwtConfigProperties jwt) {
    public record JwtConfigProperties(String audience, String issuer, String privateKey, long expirationInSeconds) {
    }
}


