package com.nabarangpur.erp.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresInMs;
    private Long userId;
    private String username;
    private String fullName;
    private Set<String> roles;
    private Set<String> privileges;
    private boolean mustChangePassword;
}
