package com.nabarangpur.erp.dto.user;

import com.nabarangpur.erp.entity.User;
import com.nabarangpur.erp.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String status;
    private boolean mustChangePassword;
    private Instant lastLoginAt;
    private Set<String> roles;
    private Instant createdAt;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .status(u.getStatus().name())
                .mustChangePassword(u.isMustChangePassword())
                .lastLoginAt(u.getLastLoginAt())
                .roles(u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(u.getCreatedAt())
                .build();
    }
}
