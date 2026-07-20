package com.nabarangpur.erp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateUserRequest {
    @Email
    private String email;

    @NotBlank
    private String fullName;

    private String phone;

    private Set<String> roleNames;
}
