package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.admission.StudentResponse;
import com.nabarangpur.erp.dto.admission.UpdateStudentRequest;
import com.nabarangpur.erp.dto.admission.UpdateStudentStatusRequest;
import com.nabarangpur.erp.dto.common.PageResponse;
import com.nabarangpur.erp.entity.AdmissionApplication;
import com.nabarangpur.erp.entity.Role;
import com.nabarangpur.erp.entity.Student;
import com.nabarangpur.erp.entity.StudentAcademic;
import com.nabarangpur.erp.entity.StudentGuardian;
import com.nabarangpur.erp.entity.StudentStatus;
import com.nabarangpur.erp.entity.StudentStatusHistory;
import com.nabarangpur.erp.entity.User;
import com.nabarangpur.erp.entity.UserStatus;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.AdmissionAcademicRepository;
import com.nabarangpur.erp.repository.AdmissionGuardianRepository;
import com.nabarangpur.erp.repository.RoleRepository;
import com.nabarangpur.erp.repository.StudentAcademicRepository;
import com.nabarangpur.erp.repository.StudentGuardianRepository;
import com.nabarangpur.erp.repository.StudentRepository;
import com.nabarangpur.erp.repository.StudentStatusHistoryRepository;
import com.nabarangpur.erp.repository.UserRepository;
import com.nabarangpur.erp.security.CustomUserDetails;
import com.nabarangpur.erp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final StudentAcademicRepository studentAcademicRepository;
    private final StudentStatusHistoryRepository statusHistoryRepository;
    private final AdmissionGuardianRepository admissionGuardianRepository;
    private final AdmissionAcademicRepository admissionAcademicRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    private static final String ALLOWED_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    /**
     * Admission approve hone par student + login user create karta hai.
     * AdmissionService.approve() is method ko call karta hai.
     */
    @Transactional
    public Student createFromAdmission(AdmissionApplication application) {

    	String studentCode = generateStudentCode(application.getCourse().getCode());

    	Role studentRole = roleRepository.findByName("STUDENT")
    	        .orElseThrow(() -> new IllegalStateException(
    	                "STUDENT role not found"));

    	String tempPassword = generateTemporaryPassword();

    	String username = studentCode.toLowerCase();

    	System.out.println("Generated Student Code = " + studentCode);
    	System.out.println("Generated Username = " + username);
    	System.out.println("Username Exists = " + userRepository.existsByUsername(username));

    	User loginUser = User.builder()
    	        .username(username)
    	        .email(application.getEmail())
    	        .fullName(application.getFullName())
    	        .passwordHash(passwordEncoder.encode(tempPassword))
    	        .status(UserStatus.ACTIVE)
    	        .mustChangePassword(true)
    	        .roles(Set.of(studentRole))
    	        .build();
    	System.out.println("========== DEBUG ==========");
    	System.out.println("Student Code = " + studentCode);
    	System.out.println("Username     = " + loginUser.getUsername());
    	System.out.println("Email        = " + loginUser.getEmail());
    	System.out.println("===========================");
    	loginUser = userRepository.save(loginUser);
        Student student = Student.builder()
                .studentCode(studentCode)
                .admissionApplication(application)
                .user(loginUser)
                .fullName(application.getFullName())
                .gender(application.getGender())
                .dateOfBirth(application.getDateOfBirth())
                .mobileNumber(application.getMobileNumber())
                .email(application.getEmail())
                .address(application.getAddress())
                .category(application.getCategory())
                .course(application.getCourse())
                .department(application.getCourse().getDepartment())
                .currentSemester(application.getEntrySemester())
                .academicYear(application.getAcademicYear())
                .status(StudentStatus.ADMITTED)
                .build();

        student = studentRepository.save(student);

        // Guardian details copy karo
        final Student savedStudent = student;
        admissionGuardianRepository.findByApplicationId(application.getId())
                .ifPresent(g -> studentGuardianRepository.save(
                        StudentGuardian.builder()
                                .student(savedStudent)
                                .fatherName(g.getFatherName())
                                .motherName(g.getMotherName())
                                .guardianName(g.getGuardianName())
                                .guardianContact(g.getGuardianContact())
                                .occupation(g.getOccupation())
                                .annualIncome(g.getAnnualIncome())
                                .build()));

        // Academic details copy karo
        admissionAcademicRepository.findByApplicationId(application.getId())
                .ifPresent(a -> studentAcademicRepository.save(
                        StudentAcademic.builder()
                                .student(savedStudent)
                                .previousInstitution(a.getPreviousInstitution())
                                .qualification(a.getQualification())
                                .boardUniversity(a.getBoardUniversity())
                                .yearOfPassing(a.getYearOfPassing())
                                .marksPercentage(a.getMarksPercentage())
                                .build()));

        recordStatusHistory(student, null, StudentStatus.ADMITTED,
                "Admission se create kiya gaya: " + application.getAdmissionRefNo());

        return student;
    }

    @Transactional
    public StudentResponse updateStatus(Long studentId, UpdateStudentStatusRequest req) {
        Student student = getStudentOrThrow(studentId);
        StudentStatus from = student.getStatus();
        validateTransition(from, req.getStatus());

        student.setStatus(req.getStatus());
        studentRepository.save(student);
        recordStatusHistory(student, from, req.getStatus(), req.getRemarks());
        auditService.record("STUDENT_STATUS_CHANGE", "Student", student.getId(), from, req.getStatus());
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse update(Long studentId, UpdateStudentRequest req) {
        Student student = getStudentOrThrow(studentId);
        StudentResponse before = StudentResponse.from(student);

        student.setFullName(req.getFullName());
        student.setMobileNumber(req.getMobileNumber());
        student.setEmail(req.getEmail());
        student.setAddress(req.getAddress());
        student.setCategory(req.getCategory());

        student = studentRepository.save(student);
        auditService.record("STUDENT_UPDATE", "Student", student.getId(),
                before, StudentResponse.from(student));
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long studentId) {
        Student student = getStudentOrThrow(studentId);
        enforceSelfAccessIfStudent(student);
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getMyProfile() {
        CustomUserDetails principal = SecurityUtils.currentUserDetails();
        Student student = studentRepository
                .findByUserIdAndDeletedFalse(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Is account se koi student profile linked nahi hai"));
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    public Student currentStudentEntity() {
        CustomUserDetails principal = SecurityUtils.currentUserDetails();
        return studentRepository
                .findByUserIdAndDeletedFalse(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Is account se koi student profile linked nahi hai"));
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> search(String search, Long departmentId, Pageable pageable) {
        Page<Student> page;
        if (search != null && !search.isBlank()) {
            page = studentRepository.search(search, pageable);
        } else if (departmentId != null) {
            page = studentRepository.findByDeletedFalseAndDepartmentId(departmentId, pageable);
        } else {
            page = studentRepository.findByDeletedFalse(pageable);
        }
        return PageResponse.from(page.map(StudentResponse::from));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void enforceSelfAccessIfStudent(Student student) {
        if (SecurityUtils.isStudent()) {
            Long currentUserId = SecurityUtils.currentUserId();
            if (student.getUser() == null
                    || !student.getUser().getId().equals(currentUserId)) {
                throw new BadRequestException("Student sirf apna profile dekh sakta hai");
            }
        }
    }

    private void validateTransition(StudentStatus from, StudentStatus to) {
        boolean valid;
        switch (from) {
            case ADMITTED:
                valid = (to == StudentStatus.ACTIVE || to == StudentStatus.WITHDRAWN);
                break;
            case ACTIVE:
                valid = (to == StudentStatus.SUSPENDED
                        || to == StudentStatus.GRADUATED
                        || to == StudentStatus.WITHDRAWN);
                break;
            case SUSPENDED:
                valid = (to == StudentStatus.ACTIVE || to == StudentStatus.WITHDRAWN);
                break;
            default:
                valid = false;
        }
        if (!valid) {
            throw new BadRequestException(
                    "Student status " + from + " se " + to + " mein change nahi ho sakta");
        }
    }

    private void recordStatusHistory(Student student, StudentStatus from,
                                     StudentStatus to, String remarks) {
        statusHistoryRepository.save(
                StudentStatusHistory.builder()
                        .student(student)
                        .fromStatus(from)
                        .toStatus(to)
                        .remarks(remarks)
                        .changedBy(currentUserOrNull())
                        .build());
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Student nahi mila: " + id));
    }

    private String generateStudentCode(String courseCode) {

        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
        int seq = 1;

        while (true) {

            String candidate = String.format("%s%s%04d", courseCode, year, seq);

            boolean studentExists =
                    studentRepository.findByStudentCodeAndDeletedFalse(candidate).isPresent();

            boolean userExists =
                    userRepository.existsByUsername(candidate.toLowerCase());

            if (!studentExists && !userExists) {
                return candidate;
            }

            seq++;
        }
    }
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("Std#");
        for (int i = 0; i < 8; i++) {
            sb.append(ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length())));
        }
        return sb.toString();
    }

    private User currentUserOrNull() {
        try {
            return SecurityUtils.currentDomainUser();
        } catch (Exception e) {
            return null;
        }
    }
}
