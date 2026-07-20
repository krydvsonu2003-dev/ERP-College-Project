package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.MarkComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarkComponentRepository extends JpaRepository<MarkComponent, Long> {
    Optional<MarkComponent> findByCode(String code);
}
