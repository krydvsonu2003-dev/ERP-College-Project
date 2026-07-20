package com.nabarangpur.erp.dto.user;

import com.nabarangpur.erp.entity.Privilege;
import com.nabarangpur.erp.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private boolean systemRole;
    private Set<String> privilegeCodes;

    public static RoleResponse from(Role r) {
        return RoleResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .systemRole(r.isSystemRole())
                .privilegeCodes(r.getPrivileges().stream().map(Privilege::getCode).collect(Collectors.toSet()))
                .build();
    }
}
