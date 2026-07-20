package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.attendance.*;
import com.nabarangpur.erp.entity.*;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.*;
import com.nabarangpur.erp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceEditHistoryRepository editHistoryRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;
    private final StudentClassEnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final SubjectRepository subjectRepository;
    private final AuditService auditService;

    private static final int CUTOFF_DAYS = 2; // marking older than this requires HOD/Admin approval
    private static final int EDIT_WINDOW_DAYS = 7; // attendance can be edited within this window

    @Transactional
    public AttendanceSessionResponse markAttendance(MarkAttendanceRequest req) {
        ClassSection section = classSectionRepository.findById(req.getClassSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Class section not found"));
        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        FacultyProfile faculty = currentFacultyOrThrow();

        enforceFacultyScopeOrPrivileged(faculty, subject.getId(), section.getId());

        boolean requiresApproval = req.getAttendanceDate().isBefore(LocalDate.now().minusDays(CUTOFF_DAYS));

        AttendanceSession session = sessionRepository
                .findByClassSectionIdAndSubjectIdAndAttendanceDateAndSessionNumber(
                        section.getId(), subject.getId(), req.getAttendanceDate(), req.getSessionNumber())
                .orElseGet(() -> sessionRepository.save(AttendanceSession.builder()
                        .classSection(section)
                        .subject(subject)
                        .faculty(faculty)
                        .attendanceDate(req.getAttendanceDate())
                        .sessionNumber(req.getSessionNumber())
                        .requiresApproval(requiresApproval)
                        .build()));

        User marker = SecurityUtils.currentDomainUser();

        for (MarkAttendanceRequest.Entry entry : req.getEntries()) {
        	Student student = studentRepository.findByIdAndDeletedFalse(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entry.getStudentId()));

            // One attendance record per student per class session - upsert, never duplicate.
            var existing = recordRepository.findBySessionIdAndStudentId(session.getId(), student.getId());
            if (existing.isPresent()) {
                AttendanceRecord rec = existing.get();
                if (rec.getStatus() != entry.getStatus()) {
                    editHistoryRepository.save(AttendanceEditHistory.builder()
                            .record(rec)
                            .oldStatus(rec.getStatus())
                            .newStatus(entry.getStatus())
                            .reason("Re-submitted during same marking session")
                            .editedBy(marker)
                            .build());
                    rec.setStatus(entry.getStatus());
                    rec.setRemarks(entry.getRemarks());
                    recordRepository.save(rec);
                }
            } else {
                recordRepository.save(AttendanceRecord.builder()
                        .session(session)
                        .student(student)
                        .status(entry.getStatus())
                        .remarks(entry.getRemarks())
                        .markedBy(marker)
                        .build());
            }
        }

        auditService.record("ATTENDANCE_MARK", "AttendanceSession", session.getId(), null,
                "Marked " + req.getEntries().size() + " students");

        return AttendanceSessionResponse.from(session, recordRepository.findBySessionId(session.getId()));
    }

    @Transactional
    public void editAttendanceRecord(Long recordId, EditAttendanceRequest req) {
        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        LocalDate sessionDate = record.getSession().getAttendanceDate();
        boolean withinWindow = !sessionDate.isBefore(LocalDate.now().minusDays(EDIT_WINDOW_DAYS));
        if (!withinWindow && !SecurityUtils.isPrivileged()) {
            throw new BadRequestException("Attendance correction window has expired; HOD/Admin approval is required");
        }

        AttendanceStatus oldStatus = record.getStatus();
        editHistoryRepository.save(AttendanceEditHistory.builder()
                .record(record)
                .oldStatus(oldStatus)
                .newStatus(req.getNewStatus())
                .reason(req.getReason())
                .editedBy(SecurityUtils.currentDomainUser())
                .build());

        record.setStatus(req.getNewStatus());
        recordRepository.save(record);
        auditService.record("ATTENDANCE_EDIT", "AttendanceRecord", record.getId(), oldStatus, req.getNewStatus());
    }

    @Transactional
    public void approveSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));
        session.setApprovedBy(SecurityUtils.currentDomainUser());
        session.setApprovedAt(java.time.Instant.now());
        session.setRequiresApproval(false);
        sessionRepository.save(session);
        auditService.record("ATTENDANCE_APPROVE", "AttendanceSession", session.getId(), null, "Approved");
    }

    @Transactional(readOnly = true)
    public AttendanceSessionResponse getSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));
        return AttendanceSessionResponse.from(session, recordRepository.findBySessionId(sessionId));
    }

    /** Students may only view their own attendance (spec 6.3.4). */
    @Transactional(readOnly = true)
    public StudentAttendanceSummary getStudentSummary(Long studentId, LocalDate from, LocalDate to) {
    	if (SecurityUtils.isStudent()) {
    	    Long currentUserId = SecurityUtils.currentUserId();
    	    Student student = studentRepository.findByIdAndDeletedFalse(studentId)
    	            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    	    if (student.getUser() == null || !student.getUser().getId().equals(currentUserId)) {
    	        throw new BadRequestException("Students may only view their own attendance");
    	    }
    	}
        LocalDate start = from != null ? from : LocalDate.now().minusMonths(6);
        LocalDate end = to != null ? to : LocalDate.now();

        List<AttendanceRecord> records = recordRepository.findByStudentAndDateRange(studentId, start, end);
        Map<AttendanceStatus, Long> counts = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

        long total = records.size();
        long present = counts.getOrDefault(AttendanceStatus.PRESENT, 0L);
        long absent = counts.getOrDefault(AttendanceStatus.ABSENT, 0L);
        long late = counts.getOrDefault(AttendanceStatus.LATE, 0L);
        long excused = counts.getOrDefault(AttendanceStatus.EXCUSED, 0L);

        double percentage = total == 0 ? 0.0 : (present + late) * 100.0 / total;

        Student student = studentRepository.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return StudentAttendanceSummary.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .totalSessions(total)
                .present(present)
                .absent(absent)
                .late(late)
                .excused(excused)
                .attendancePercentage(Math.round(percentage * 100) / 100.0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> facultySessionsForDate(LocalDate date) {
        FacultyProfile faculty = currentFacultyOrThrow();
        return sessionRepository.findByFacultyIdAndAttendanceDate(faculty.getId(), date).stream()
                .map(s -> AttendanceSessionResponse.from(s, recordRepository.findBySessionId(s.getId())))
                .collect(Collectors.toList());
    }

    private FacultyProfile currentFacultyOrThrow() {
        Long userId = SecurityUtils.currentUserId();
        return facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Current user has no faculty profile"));
    }

    private void enforceFacultyScopeOrPrivileged(FacultyProfile faculty, Long subjectId, Long classSectionId) {
        if (SecurityUtils.isPrivileged()) return;
        boolean assigned = assignmentRepository.existsByFacultyIdAndSubjectIdAndClassSectionId(
                faculty.getId(), subjectId, classSectionId);
        if (!assigned) {
            throw new BadRequestException("You are not assigned to teach this subject/class section");
        }
    }
}
