package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Optional<Privilege> findByCode(String code);
}
