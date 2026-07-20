package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.admission.StudentResponse;
import com.nabarangpur.erp.entity.ClassSection;
import com.nabarangpur.erp.entity.Student;
import com.nabarangpur.erp.entity.StudentClassEnrollment;
import com.nabarangpur.erp.exception.ConflictException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.ClassSectionRepository;
import com.nabarangpur.erp.repository.StudentClassEnrollmentRepository;
import com.nabarangpur.erp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/** Maps students to class sections so faculty can load a class roster for attendance/marks. */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final StudentClassEnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final AuditService auditService;

    @Transactional
    public void enroll(Long studentId, Long classSectionId, Integer semester) {
    	System.out.println("Enroll method entered");
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        ClassSection section = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Class section not found"));

        boolean exists = enrollmentRepository.findByClassSectionIdAndSemester(classSectionId, semester).stream()
                .anyMatch(e -> e.getStudent().getId().equals(studentId));
        if (exists) {
            throw new ConflictException("Student is already enrolled in this class section for this semester");
        }

        StudentClassEnrollment enrollment = StudentClassEnrollment.builder()
                .student(student).classSection(section).semester(semester)
                .enrolledAt(Instant.now()).build();
        enrollmentRepository.save(enrollment);
        System.out.println("Enrollment Saved = " + enrollment.getId());
        auditService.record("STUDENT_ENROLL", "StudentClassEnrollment", enrollment.getId(), null,
                "Enrolled " + student.getStudentCode() + " in section " + classSectionId);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> rosterFor(Long classSectionId, Integer semester) {

        List<StudentClassEnrollment> list =
                enrollmentRepository.findByClassSectionIdAndSemester(classSectionId, semester);

        System.out.println("========== ROSTER ==========");
        System.out.println("Roster Size = " + list.size());

        list.forEach(e -> {
            System.out.println(
                    "Student ID = " + e.getStudent().getId()
                    + " | Name = " + e.getStudent().getFullName()
            );
        });

        return list.stream()
                .map(e -> StudentResponse.from(e.getStudent()))
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteEnrollment(Long studentId) {
        enrollmentRepository.deleteByStudentId(studentId);
    }
}
