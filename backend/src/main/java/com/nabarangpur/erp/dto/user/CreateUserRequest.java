package com.nabarangpur.erp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String fullName;

    private String phone;

    @NotEmpty(message = "At least one role must be assigned")
    private Set<String> roleNames;
}
