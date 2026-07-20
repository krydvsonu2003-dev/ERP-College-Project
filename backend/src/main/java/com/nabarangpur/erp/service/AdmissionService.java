package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.admission.*;
import com.nabarangpur.erp.dto.common.PageResponse;
import com.nabarangpur.erp.entity.*;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.InvalidWorkflowStateException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.*;
import com.nabarangpur.erp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdmissionService {
	private final AttendanceRecordRepository attendanceRecordRepository;
	private final ResultCardRepository resultCardRepository;
	private final ResultCardRevisionRepository resultCardRevisionRepository;
	private final ExamMarkRepository examMarkRepository;
	private final SemesterResultRepository semesterResultRepository;
	private final FeeInvoiceRepository feeInvoiceRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentAllocationRepository paymentAllocationRepository;
	private final StudentRepository studentRepository;
	private final ReceiptRepository receiptRepository;
	private final StudentFeeAssignmentRepository studentFeeAssignmentRepository;
	private final AdmissionApplicationRepository applicationRepository;
	private final AdmissionDocumentRepository documentRepository;
	private final AdmissionGuardianRepository guardianRepository;
	private final AdmissionAcademicRepository academicRepository;
	private final AdmissionStatusHistoryRepository statusHistoryRepository;
	private final CourseRepository courseRepository;
	

	private final RefundTransactionRepository refundTransactionRepository;
	private final AcademicYearRepository academicYearRepository;
	private final StudentService studentService;
	private final AuditService auditService;


	private final ClassSectionRepository classSectionRepository;
	private final EnrollmentService enrollmentService;

    @org.springframework.beans.factory.annotation.Value("${app.files.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    @Transactional
    public AdmissionResponse submit(SubmitAdmissionRequest req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        AcademicYear year = academicYearRepository.findById(req.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        AdmissionApplication application = AdmissionApplication.builder()
                .admissionRefNo(generateRefNo())
                .fullName(req.getFullName())
                .gender(req.getGender())
                .dateOfBirth(req.getDateOfBirth())
                .mobileNumber(req.getMobileNumber())
                .email(req.getEmail())
                .address(req.getAddress())
                .category(req.getCategory())
                .idProofNumber(req.getIdProofNumber())
                .course(course)
                .academicYear(year)
                .entrySemester(req.getEntrySemester())
                .status(AdmissionStatus.SUBMITTED)
                .submittedBy(currentUserOrNull())
                .build();
        application = applicationRepository.save(application);

        guardianRepository.save(AdmissionGuardian.builder()
                .application(application)
                .fatherName(req.getFatherName())
                .motherName(req.getMotherName())
                .guardianName(req.getGuardianName())
                .guardianContact(req.getGuardianContact())
                .occupation(req.getOccupation())
                .annualIncome(req.getAnnualIncome())
                .build());

        academicRepository.save(AdmissionAcademic.builder()
                .application(application)
                .previousInstitution(req.getPreviousInstitution())
                .qualification(req.getQualification())
                .boardUniversity(req.getBoardUniversity())
                .yearOfPassing(req.getYearOfPassing())
                .marksPercentage(req.getMarksPercentage())
                .build());

        recordStatusHistory(application, null, AdmissionStatus.SUBMITTED, "Application submitted");
        auditService.record("ADMISSION_SUBMIT", "AdmissionApplication", application.getId(), null,
                AdmissionResponse.from(application));

        return AdmissionResponse.from(application);
    }

    @Transactional
    public AdmissionDocumentInfo uploadDocument(Long applicationId, String documentType, MultipartFile file) {
        AdmissionApplication application = getApplicationOrThrow(applicationId);

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("Unsupported file type. Allowed: " + ALLOWED_EXTENSIONS);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File exceeds the 10MB size limit");
        }

        try {
            Path dir = Paths.get(uploadDir, "admissions", String.valueOf(applicationId));
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID() + "." + extension;
            Path target = dir.resolve(storedName);
            Files.copy(file.getInputStream(), target);

            AdmissionDocument doc = AdmissionDocument.builder()
                    .application(application)
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .filePath(target.toString())
                    .contentType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .build();
            doc = documentRepository.save(doc);
            return new AdmissionDocumentInfo(doc.getId(), doc.getDocumentType(), doc.getFileName());
        } catch (IOException e) {
            throw new BadRequestException("Failed to store document: " + e.getMessage());
        }
    }

    public record AdmissionDocumentInfo(Long id, String documentType, String fileName) {}

    @Transactional
    public AdmissionResponse markUnderReview(Long applicationId, VerifyAdmissionRequest req) {
        AdmissionApplication application = getApplicationOrThrow(applicationId);
        if (application.getStatus() != AdmissionStatus.SUBMITTED) {
            throw new InvalidWorkflowStateException("Only SUBMITTED applications can move to UNDER_REVIEW");
        }
        AdmissionStatus from = application.getStatus();
        application.setStatus(AdmissionStatus.UNDER_REVIEW);
        application.setReviewedBy(currentUserOrNull());
        application.setReviewedAt(Instant.now());
        applicationRepository.save(application);

        recordStatusHistory(application, from, AdmissionStatus.UNDER_REVIEW, req.getRemarks());
        auditService.record("ADMISSION_REVIEW", "AdmissionApplication", application.getId(), from,
                application.getStatus());
        return AdmissionResponse.from(application);
    }

    @Transactional
    public StudentResponse approve(Long applicationId) {
        AdmissionApplication application = getApplicationOrThrow(applicationId);
        if (application.getStatus() != AdmissionStatus.SUBMITTED && application.getStatus() != AdmissionStatus.UNDER_REVIEW) {
            throw new InvalidWorkflowStateException("Only SUBMITTED or UNDER_REVIEW applications can be approved");
        }
        AdmissionStatus from = application.getStatus();
        application.setStatus(AdmissionStatus.APPROVED);
        application.setReviewedBy(currentUserOrNull());
        application.setReviewedAt(Instant.now());
        applicationRepository.save(application);
        recordStatusHistory(application, from, AdmissionStatus.APPROVED, "Application approved");

        // Approval creates a student record with mapped admission reference.
        Student student = studentService.createFromAdmission(application);

        System.out.println("Student Created = " + student.getId());

        Long classSectionId = classSectionRepository
                .findByCourseIdAndSemester(
                        application.getCourse().getId(),
                        application.getEntrySemester())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Class Section not found"))
                .getId();

        System.out.println("Class Section = " + classSectionId);

        System.out.println("Calling enroll...");

        enrollmentService.enroll(
                student.getId(),
                classSectionId,
                application.getEntrySemester()
        );

        System.out.println("Enroll completed");

        application.setStatus(AdmissionStatus.STUDENT_CREATED);
        applicationRepository.save(application);
        recordStatusHistory(application, AdmissionStatus.APPROVED, AdmissionStatus.STUDENT_CREATED,
                "Student record " + student.getStudentCode() + " created");

        auditService.record("ADMISSION_APPROVE", "AdmissionApplication", application.getId(), from,
                "Student created: " + student.getStudentCode());

        return StudentResponse.from(student);
    }

    @Transactional
    public AdmissionResponse reject(Long applicationId, RejectAdmissionRequest req) {
        AdmissionApplication application = getApplicationOrThrow(applicationId);
        if (application.getStatus() == AdmissionStatus.STUDENT_CREATED) {
            throw new InvalidWorkflowStateException("Cannot reject an application that already created a student");
        }
        AdmissionStatus from = application.getStatus();
        application.setStatus(AdmissionStatus.REJECTED);
        application.setRejectionRemarks(req.getRemarks());
        application.setReviewedBy(currentUserOrNull());
        application.setReviewedAt(Instant.now());
        applicationRepository.save(application);

        recordStatusHistory(application, from, AdmissionStatus.REJECTED, req.getRemarks());
        auditService.record("ADMISSION_REJECT", "AdmissionApplication", application.getId(), from, req.getRemarks());
        return AdmissionResponse.from(application);
    }

    @Transactional(readOnly = true)
    public AdmissionResponse get(Long id) {
        return AdmissionResponse.from(getApplicationOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdmissionResponse> list(AdmissionStatus status, Pageable pageable) {
    	Page<AdmissionApplication> page = (status == null)
    	        ? applicationRepository.findByDeletedFalse(pageable)
    	        : applicationRepository.findByStatusAndDeletedFalse(status, pageable);
        return PageResponse.from(page.map(AdmissionResponse::from));
    }

    @Transactional(readOnly = true)
    public List<AdmissionStatusHistory> history(Long applicationId) {
        return statusHistoryRepository.findByApplicationIdOrderByChangedAtAsc(applicationId);
    }
   
    @Transactional
    public void deleteAdmission(Long id) {

        AdmissionApplication application = getApplicationOrThrow(id);

        application.setDeleted(true);

        Student student = studentRepository
                .findByAdmissionApplicationId(application.getId())
                .orElse(null);

        if (student != null) {

            Long studentId = student.getId();

            // Attendance
            attendanceRecordRepository.deleteByStudentId(studentId);

            // Result Revision
            resultCardRevisionRepository.deleteByResultCardStudentId(studentId);

            // Result Card
            resultCardRepository.deleteByStudentId(studentId);

            // Exam Marks
            examMarkRepository.deleteByStudentId(studentId);

            // Semester Result
            semesterResultRepository.deleteByStudentId(studentId);

            List<Payment> payments = paymentRepository.findByStudentId(studentId);

            for (Payment payment : payments) {

                paymentAllocationRepository.deleteByPaymentId(payment.getId());

                receiptRepository.deleteByPaymentId(payment.getId());

                refundTransactionRepository.deleteByPaymentId(payment.getId());
            }

            // Delete Payments
            paymentRepository.deleteByStudentId(studentId);

            // Delete Fee Invoice
            feeInvoiceRepository.deleteByStudentId(studentId);

            // Delete Student Fee Assignment  <-- ADD THIS
            studentFeeAssignmentRepository.deleteByStudentId(studentId);

            // Enrollment
            enrollmentService.deleteEnrollment(studentId);

            // Soft delete student
            student.setDeleted(true);
            studentRepository.save(student);
        }

        applicationRepository.save(application);

        auditService.record(
                "ADMISSION_DELETE",
                "AdmissionApplication",
                id,
                null,
                "Soft deleted"
        );
    }
    
    private void recordStatusHistory(AdmissionApplication application, AdmissionStatus from, AdmissionStatus to, String remarks) {
        statusHistoryRepository.save(AdmissionStatusHistory.builder()
                .application(application)
                .fromStatus(from)
                .toStatus(to)
                .remarks(remarks)
                .changedBy(currentUserOrNull())
                .changedAt(Instant.now())
                .build());
    }

    private AdmissionApplication getApplicationOrThrow(Long id) {
    	return applicationRepository.findByIdAndDeletedFalse(id)
    	        .orElseThrow(() -> new ResourceNotFoundException("Admission application not found: " + id));
    }

    private String generateRefNo() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
        SecureRandom random = new SecureRandom();
        String seq = String.format("%06d", random.nextInt(1_000_000));
        String candidate = "ADM" + year + seq;
        // Practically unique given the random sequence + DB unique constraint as a safety net.
        return applicationRepository.findByAdmissionRefNo(candidate).isPresent()
                ? generateRefNo()
                : candidate;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private User currentUserOrNull() {
        try {
            return SecurityUtils.currentDomainUser();
        } catch (Exception e) {
            return null;
        }
    }
}
