package com.nabarangpur.erp.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateUserResult {
    private UserResponse user;
    private String temporaryPassword;
}
