package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.user.AssignPrivilegesRequest;
import com.nabarangpur.erp.dto.user.CreateRoleRequest;
import com.nabarangpur.erp.dto.user.RoleResponse;
import com.nabarangpur.erp.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role & Privilege Management", description = "Functional Spec 6.1")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.ok(roleService.listRoles());
    }

    @GetMapping("/privileges")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<String>> listPrivileges() {
        return ApiResponse.ok(roleService.listPrivilegeCodes());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.ok("Role created", roleService.createRole(request));
    }

    @PutMapping("/{id}/privileges")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<RoleResponse> assignPrivileges(@PathVariable Long id,
                                                       @Valid @RequestBody AssignPrivilegesRequest request) {
        return ApiResponse.ok("Privileges updated", roleService.assignPrivileges(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.message("Role deleted");
    }
}
