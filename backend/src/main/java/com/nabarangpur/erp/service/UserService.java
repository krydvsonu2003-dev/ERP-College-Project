package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.common.PageResponse;
import com.nabarangpur.erp.dto.user.CreateUserRequest;
import com.nabarangpur.erp.dto.user.CreateUserResult;
import com.nabarangpur.erp.dto.user.UpdateUserRequest;
import com.nabarangpur.erp.dto.user.UserResponse;
import com.nabarangpur.erp.entity.Role;
import com.nabarangpur.erp.entity.User;
import com.nabarangpur.erp.entity.UserStatus;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.ConflictException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.RoleRepository;
import com.nabarangpur.erp.repository.UserRepository;
import com.nabarangpur.erp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    private static final String ALLOWED_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";

    @Transactional
    public CreateUserResult createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        Set<Role> roles = resolveRoles(request.getRoleNames());
        String tempPassword = generateTemporaryPassword();

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .roles(roles)
                .createdBy(currentUserIdOrNull())
                .build();

        user = userRepository.save(user);
        auditService.record("USER_CREATE", "User", user.getId(), null, UserResponse.from(user));

        
        return CreateUserResult.builder()
                .user(UserResponse.from(user))
                .temporaryPassword(tempPassword)
                .build();
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = getUserOrThrow(userId);
        UserResponse before = UserResponse.from(user);

        if (request.getEmail() != null) user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoleNames()));
        }
        user.setUpdatedBy(currentUserIdOrNull());

        user = userRepository.save(user);
        auditService.record("USER_UPDATE", "User", user.getId(), before, UserResponse.from(user));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse setActiveStatus(Long userId, boolean active) {
        User user = getUserOrThrow(userId);
        guardAgainstSelfLockoutOfLastSuperAdmin(user, active);

        UserResponse before = UserResponse.from(user);
        user.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        if (active) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
        user = userRepository.save(user);
        auditService.record(active ? "USER_ACTIVATE" : "USER_DEACTIVATE", "User", user.getId(),
                before, UserResponse.from(user));
        return UserResponse.from(user);
    }

    @Transactional
    public String resetPassword(Long userId) {
        User user = getUserOrThrow(userId);
        String tempPassword = generateTemporaryPassword();
        // Password reset must invalidate the old password immediately.
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        user.setFailedLoginAttempts(0);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
            user.setLockedUntil(null);
        }
        userRepository.save(user);
        auditService.record("USER_PASSWORD_RESET_BY_ADMIN", "User", user.getId(), null, null);
        return tempPassword;
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return UserResponse.from(getUserOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(String search, Pageable pageable) {
        Page<User> page = (search == null || search.isBlank())
                ? userRepository.findByDeletedFalse(pageable)
                : userRepository.search(search, pageable);
        return PageResponse.from(page.map(UserResponse::from));
    }
    @Transactional
    public void deleteUser(Long userId) {

        User user = getUserOrThrow(userId);

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));

        if (isSuperAdmin) {
            long activeSuperAdmins = userRepository.findAll().stream()
                    .filter(u -> !u.isDeleted())
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(r -> "SUPER_ADMIN".equals(r.getName())))
                    .count();

            if (activeSuperAdmins <= 1) {
                throw new BadRequestException("Cannot delete the last Super Admin");
            }
        }

        user.setDeleted(true);
        user.setUpdatedBy(currentUserIdOrNull());
        userRepository.save(user);

        auditService.record(
                "USER_DELETE",
                "User",
                user.getId(),
                UserResponse.from(user),
                null
        );
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            roles.add(roleRepository.findByName(name)
                    .orElseThrow(() -> new BadRequestException("Unknown role: " + name)));
        }
        return roles;
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private void guardAgainstSelfLockoutOfLastSuperAdmin(User user, boolean active) {
        boolean isSuperAdmin = user.getRoles().stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));
        if (isSuperAdmin && !active) {
            long activeSuperAdmins = userRepository.findAll().stream()
                    .filter(u -> !u.isDeleted() && u.getStatus() == UserStatus.ACTIVE)
                    .filter(u -> u.getRoles().stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getName())))
                    .count();
            if (activeSuperAdmins <= 1) {
                throw new BadRequestException("Cannot deactivate the last active Super Admin account");
            }
        }
    }

    private Long currentUserIdOrNull() {
        try {
            return SecurityUtils.currentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("Tmp#");
        for (int i = 0; i < 10; i++) {
            sb.append(ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length())));
        }
        return sb.toString();
    }
}
