package com.nabarangpur.erp.controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.common.PageResponse;
import com.nabarangpur.erp.dto.user.*;
import com.nabarangpur.erp.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Functional Spec 6.1 - create/edit/(de)activate/reset-password")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ApiResponse<CreateUserResult> createUser(@Valid @RequestBody CreateUserRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("========== USER CONTROLLER ==========");
        System.out.println("Authentication : " + auth);

        if (auth != null) {
            System.out.println("Principal : " + auth.getPrincipal());
            System.out.println("Authorities : " + auth.getAuthorities());
        } else {
            System.out.println("Authentication is NULL");
        }

        return ApiResponse.ok(
                "User created. Share the temporary password securely.",
                userService.createUser(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUser(id));
    }
    

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<PageResponse<UserResponse>> searchUsers(@RequestParam(required = false) String search,
                                                                Pageable pageable) {
        return ApiResponse.ok(userService.searchUsers(search, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok("User updated", userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<UserResponse> setStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ApiResponse.ok(active ? "User activated" : "User deactivated", userService.setActiveStatus(id, active));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<Map<String, String>> resetPassword(@PathVariable Long id) {
        String tempPassword = userService.resetPassword(id);
        return ApiResponse.ok("Password reset. Share the temporary password securely.",
                Map.of("temporaryPassword", tempPassword));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("DELETE METHOD CALLED");
        System.out.println(auth.getAuthorities());

        userService.deleteUser(id);
        return ApiResponse.ok("User deleted successfully", null);
    }
}
