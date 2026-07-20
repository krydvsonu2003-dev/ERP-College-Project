package com.nabarangpur.erp.security;

import com.nabarangpur.erp.entity.Privilege;
import com.nabarangpur.erp.entity.Role;
import com.nabarangpur.erp.entity.User;
import com.nabarangpur.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        System.out.println("Loading User : " + username);

        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        System.out.println("User Loaded");

        System.out.println("Roles Count = " + user.getRoles().size());

        for (Role role : user.getRoles()) {

            System.out.println("Role = " + role.getName());

            for (Privilege p : role.getPrivileges()) {
                System.out.println("Privilege = " + p.getCode());
            }
        }

        return new CustomUserDetails(user);
    }
}
