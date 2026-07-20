package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByClassSectionIdAndSubjectIdAndAttendanceDateAndSessionNumber(
            Long classSectionId, Long subjectId, LocalDate attendanceDate, Integer sessionNumber);

    List<AttendanceSession> findByFacultyIdAndAttendanceDate(Long facultyId, LocalDate attendanceDate);

    List<AttendanceSession> findByClassSectionIdAndSubjectIdAndAttendanceDateBetween(
            Long classSectionId, Long subjectId, LocalDate from, LocalDate to);
}
