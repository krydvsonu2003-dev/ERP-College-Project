package com.nabarangpur.erp.repository;
import org.springframework.transaction.annotation.Transactional;
import com.nabarangpur.erp.entity.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    List<StudentStatusHistory> findByStudentIdOrderByChangedAtAsc(Long studentId);
    
    @Transactional
    void deleteByStudentId(Long studentId);
}
