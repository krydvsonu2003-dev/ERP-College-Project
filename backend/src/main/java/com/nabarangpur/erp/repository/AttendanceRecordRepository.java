package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AttendanceRecord;
import com.nabarangpur.erp.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    List<AttendanceRecord> findBySessionId(Long sessionId);

    @Query("SELECT r FROM AttendanceRecord r " +
    	       "WHERE r.student.id = :studentId " +
    	       "AND r.student.deleted = false " +
    	       "AND r.session.attendanceDate >= :fromDate " +
    	       "AND r.session.attendanceDate <= :toDate")
    	List<AttendanceRecord> findByStudentAndDateRange(
    	        @Param("studentId") Long studentId,
    	        @Param("fromDate") LocalDate fromDate,
    	        @Param("toDate") LocalDate toDate);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    long countByStudentId(Long studentId);
    @Transactional
    void deleteByStudentId(Long studentId);
}
