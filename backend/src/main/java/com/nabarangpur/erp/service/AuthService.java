package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.auth.*;
import com.nabarangpur.erp.entity.PasswordResetToken;
import com.nabarangpur.erp.entity.User;
import com.nabarangpur.erp.entity.UserStatus;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.exception.UnauthorizedException;
import com.nabarangpur.erp.repository.PasswordResetTokenRepository;
import com.nabarangpur.erp.repository.UserRepository;
import com.nabarangpur.erp.security.CustomUserDetails;
import com.nabarangpur.erp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Value("${app.security.max-failed-login-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.account-lock-minutes}")
    private int lockMinutes;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletedFalse(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.isLocked()) {
            throw new LockedException("Account is locked until " + user.getLockedUntil());
        }
        if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.PENDING) {
            throw new DisabledException("Account is " + user.getStatus().name().toLowerCase());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            registerFailedAttempt(user);
            throw ex;
        }

        // success: reset failed attempts, record login
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        auditService.record("USER_LOGIN", "User", user.getId(), null, null);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtUtil.getAccessTokenExpiryMs())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(userDetails.getRoleNames())
                .privileges(userDetails.getAuthorities().stream()
                        .map(Object::toString)
                        .filter(a -> !a.startsWith("ROLE_"))
                        .collect(Collectors.toSet()))
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    private void registerFailedAttempt(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(Instant.now().plusSeconds(lockMinutes * 60L));
            auditService.record("USER_AUTO_LOCKED", "User", user.getId(), null,
                    "Locked after " + user.getFailedLoginAttempts() + " failed attempts");
        }
        userRepository.save(user);
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtUtil.isTokenValid(token) || !jwtUtil.isRefreshToken(token)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccess = jwtUtil.generateAccessToken(userDetails);
        String newRefresh = jwtUtil.generateRefreshToken(userDetails);

        return LoginResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresInMs(jwtUtil.getAccessTokenExpiryMs())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(userDetails.getRoleNames())
                .privileges(userDetails.getAuthorities().stream()
                        .map(Object::toString)
                        .filter(a -> !a.startsWith("ROLE_"))
                        .collect(Collectors.toSet()))
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        // Password reset must invalidate the old password immediately.
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        auditService.record("USER_PASSWORD_CHANGE", "User", user.getId(), null, null);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByUsernameAndDeletedFalse(request.getUsername()).orElse(null);
        if (user == null) {
            // Do not reveal whether the username exists.
            return;
        }
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .build();
        passwordResetTokenRepository.save(token);
        // In production this token would be emailed/SMS'd to the user.
        auditService.record("USER_PASSWORD_RESET_REQUESTED", "User", user.getId(), null, null);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or already used reset token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        auditService.record("USER_PASSWORD_RESET", "User", user.getId(), null, null);
    }
}
