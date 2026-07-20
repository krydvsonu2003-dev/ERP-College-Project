package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AttendanceEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceEditHistoryRepository extends JpaRepository<AttendanceEditHistory, Long> {
    List<AttendanceEditHistory> findByRecordIdOrderByEditedAtAsc(Long recordId);
}
