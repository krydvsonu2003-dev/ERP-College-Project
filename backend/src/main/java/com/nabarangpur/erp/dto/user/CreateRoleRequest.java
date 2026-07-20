package com.nabarangpur.erp.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateRoleRequest {
    @NotBlank
    private String name;

    private String description;

    private Set<String> privilegeCodes;
}
