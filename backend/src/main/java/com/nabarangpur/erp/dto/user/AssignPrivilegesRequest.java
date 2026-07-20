package com.nabarangpur.erp.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AssignPrivilegesRequest {
    @NotEmpty
    private Set<String> privilegeCodes;
}
