package com.nabarangpur.erp.security;

import com.nabarangpur.erp.entity.Privilege;
import com.nabarangpur.erp.entity.Role;
import com.nabarangpur.erp.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps our domain User so Spring Security can reason about it.
 * Authorities = ROLE_<roleName> for every role, plus the raw privilege
 * code (e.g. STUDENT_CREATE) for every privilege granted via those roles.
 * This lets controllers use both hasRole(...) and hasAuthority(...) checks.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getDomainUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Privilege p : role.getPrivileges()) {
                authorities.add(new SimpleGrantedAuthority(p.getCode()));
            }
        }
        return authorities;
    }

    public Set<String> getRoleNames() {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == com.nabarangpur.erp.entity.UserStatus.ACTIVE;
    }
}
