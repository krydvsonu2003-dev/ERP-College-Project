package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.common.*;
import com.nabarangpur.erp.entity.*;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.repository.*;
import com.nabarangpur.erp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Functional Spec 6.6 - role-specific dashboard & summary views.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final AdmissionApplicationRepository admissionApplicationRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final ResultCardRepository resultCardRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceService attendanceService;
    private final FeeService feeService;

    @Transactional(readOnly = true)
    public PrincipalDashboardResponse principalDashboard() {
        long total = studentRepository.countByDeletedFalse();
        long active = studentRepository.countByStatusAndDeletedFalse(StudentStatus.ACTIVE);
        long pendingAdmissions = admissionApplicationRepository.countByStatus(AdmissionStatus.SUBMITTED)
                + admissionApplicationRepository.countByStatus(AdmissionStatus.UNDER_REVIEW);
        long pendingInvoices = feeInvoiceRepository.countByStatus(FeeInvoiceStatus.DUE)
                + feeInvoiceRepository.countByStatus(FeeInvoiceStatus.PARTIALLY_PAID)
                + feeInvoiceRepository.countByStatus(FeeInvoiceStatus.OVERDUE);

        BigDecimal totalCollected = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.POSTED)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDue = feeInvoiceRepository.findAll().stream()
                .map(FeeInvoice::getDueAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        long published = resultCardRepository.findAll().stream().filter(r -> r.getStatus() == ResultStatus.PUBLISHED).count();
        long draft = resultCardRepository.findAll().stream().filter(r -> r.getStatus() == ResultStatus.DRAFT).count();

        Map<String, Long> byDept = new HashMap<>();
        departmentRepository.findAll().forEach(d ->
                byDept.put(d.getName(), studentRepository.findByDeletedFalseAndDepartmentId(d.getId(),
                        org.springframework.data.domain.Pageable.unpaged()).getTotalElements()));

        return PrincipalDashboardResponse.builder()
                .totalStudents(total)
                .activeStudents(active)
                .pendingAdmissions(pendingAdmissions)
                .pendingFeeInvoices(pendingInvoices)
                .totalFeeCollected(totalCollected)
                .totalFeeDue(totalDue)
                .publishedResultCards(published)
                .draftResultCards(draft)
                .studentsByDepartment(byDept)
                .build();
    }

    @Transactional(readOnly = true)
    public FacultyDashboardResponse facultyDashboard() {
        FacultyProfile faculty = facultyProfileRepository.findByUserId(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BadRequestException("Current user has no faculty profile"));

        List<FacultySubjectAssignment> assignments = assignmentRepository.findByFacultyId(faculty.getId());
        List<FacultyDashboardResponse.AssignmentInfo> info = assignments.stream()
                .map(a -> FacultyDashboardResponse.AssignmentInfo.builder()
                        .subjectId(a.getSubject().getId())
                        .subjectName(a.getSubject().getName())
                        .classSectionId(a.getClassSection().getId())
                        .semester(a.getClassSection().getSemester())
                        .build())
                .collect(Collectors.toList());

        return FacultyDashboardResponse.builder()
                .assignedClassCount(info.size())
                .assignments(info)
                .build();
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse studentDashboard() {
        Student student = studentRepository.findByUserIdAndDeletedFalse(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BadRequestException("No student profile linked to this account"));

        var summary = attendanceService.getStudentSummary(student.getId(), LocalDate.now().minusMonths(12), LocalDate.now());
        long publishedResults = resultCardRepository.findByStudentIdAndStatus(student.getId(), ResultStatus.PUBLISHED).size();
        BigDecimal totalDue = feeInvoiceRepository.findByStudentId(student.getId()).stream()
                .map(FeeInvoice::getDueAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        int completeness = computeProfileCompleteness(student);

        return StudentDashboardResponse.builder()
                .attendancePercentage(summary.getAttendancePercentage())
                .publishedResultCount(publishedResults)
                .totalDueAmount(totalDue)
                .profileCompletenessPercent(completeness)
                .build();
    }

    @Transactional(readOnly = true)
    public AccountantDashboardResponse accountantDashboard() {
        BigDecimal todays = feeService.totalCollectionToday();
        long pendingDues = feeInvoiceRepository.countByStatus(FeeInvoiceStatus.DUE)
                + feeInvoiceRepository.countByStatus(FeeInvoiceStatus.PARTIALLY_PAID)
                + feeInvoiceRepository.countByStatus(FeeInvoiceStatus.OVERDUE);
        long receipts = receiptRepository.count();

        return AccountantDashboardResponse.builder()
                .todaysCollection(todays)
                .pendingDueInvoices(pendingDues)
                .receiptsGeneratedTotal(receipts)
                .build();
    }

    private int computeProfileCompleteness(Student s) {
        int filled = 0, totalFields = 6;
        if (s.getMobileNumber() != null && !s.getMobileNumber().isBlank()) filled++;
        if (s.getEmail() != null && !s.getEmail().isBlank()) filled++;
        if (s.getAddress() != null && !s.getAddress().isBlank()) filled++;
        if (s.getCategory() != null && !s.getCategory().isBlank()) filled++;
        filled++; // fullName always present
        filled++; // dateOfBirth always present
        return (int) Math.round(filled * 100.0 / totalFields);
    }
}
