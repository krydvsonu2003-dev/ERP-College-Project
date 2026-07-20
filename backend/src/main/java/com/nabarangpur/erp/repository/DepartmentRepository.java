package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
