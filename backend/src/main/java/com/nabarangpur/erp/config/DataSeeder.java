package com.nabarangpur.erp.config;

import com.nabarangpur.erp.entity.Role;
import com.nabarangpur.erp.entity.User;
import com.nabarangpur.erp.entity.UserStatus;
import com.nabarangpur.erp.repository.RoleRepository;
import com.nabarangpur.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Super Admin account create karta hai agar exist nahi karta.
 * Note: Pehle SQL Developer mein 03_seed_data.sql run karna
 * zaroori hai, tab hi SUPER_ADMIN role milega.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_USERNAME = "superadmin";
    private static final String DEFAULT_PASSWORD = "Admin@12345";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Agar superadmin already exist karta hai to kuch mat karo
        if (userRepository.existsByUsername(DEFAULT_USERNAME)) {
            log.info("Super Admin account already exists.");
            return;
        }

        // SUPER_ADMIN role dhundho (SQL se seed hona chahiye)
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElse(null);

        if (superAdminRole == null) {
            log.error("=========================================================");
            log.error("SUPER_ADMIN role nahi mila!");
            log.error("Pehle SQL Developer mein ye files run karo:");
            log.error("  1. database/01_create_user.sql  (SYSDBA se)");
            log.error("  2. database/02_create_tables.sql (erp_user se)");
            log.error("  3. database/03_seed_data.sql  (erp_user se)");
            log.error("Phir application restart karo.");
            log.error("=========================================================");
            return;
        }

        User admin = User.builder()
                .username(DEFAULT_USERNAME)
                .email("superadmin@nabarangpurpharmacy.edu")
                .fullName("Super Administrator")
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .roles(Set.of(superAdminRole))
                .build();

        userRepository.save(admin);

        log.warn("============================================================");
        log.warn(" Super Admin account banaya gaya.");
        log.warn(" Username : {}", DEFAULT_USERNAME);
        log.warn(" Password : {}", DEFAULT_PASSWORD);
        log.warn(" Login karne ke baad password zaroor badlein!");
        log.warn("============================================================");
    }
}
