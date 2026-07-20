package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.faculty.*;
import com.nabarangpur.erp.entity.*;
import com.nabarangpur.erp.exception.ConflictException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Functional Spec 6.1/6.3/6.4 - faculty operational profile and the
 * class/subject scoping that drives "Faculty can mark attendance for
 * assigned classes only" (6.3.7) and "Faculty can enter marks only for
 * assigned subjects" (6.4.6).
 */
@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyProfileRepository facultyProfileRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSectionRepository classSectionRepository;
    private final AuditService auditService;

    @Transactional
    public FacultyProfileResponse createProfile(CreateFacultyProfileRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (facultyProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new ConflictException("Faculty profile already exists for this user");
        }
        Department dept = req.getDepartmentId() != null
                ? departmentRepository.findById(req.getDepartmentId()).orElse(null)
                : null;

        FacultyProfile profile = FacultyProfile.builder()
                .user(user).department(dept)
                .employeeCode(req.getEmployeeCode())
                .designation(req.getDesignation())
                .build();
        profile = facultyProfileRepository.save(profile);
        auditService.record("FACULTY_PROFILE_CREATE", "FacultyProfile", profile.getId(), null, req);
        return FacultyProfileResponse.from(profile);
    }

    @Transactional
    public FacultySubjectAssignmentResponse assignSubject(AssignFacultySubjectRequest req) {
        FacultyProfile faculty = facultyProfileRepository.findById(req.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found"));
        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        ClassSection section = classSectionRepository.findById(req.getClassSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Class section not found"));

        if (assignmentRepository.existsByFacultyIdAndSubjectIdAndClassSectionId(faculty.getId(), subject.getId(), section.getId())) {
            throw new ConflictException("This faculty is already assigned to this subject/class section");
        }

        FacultySubjectAssignment assignment = FacultySubjectAssignment.builder()
                .faculty(faculty).subject(subject).classSection(section).build();
        assignment = assignmentRepository.save(assignment);
        auditService.record("FACULTY_SUBJECT_ASSIGN", "FacultySubjectAssignment", assignment.getId(), null, req);
        return FacultySubjectAssignmentResponse.from(assignment);
    }

    @Transactional(readOnly = true)
    public List<FacultyProfileResponse> listProfiles() {
        return facultyProfileRepository.findAll().stream().map(FacultyProfileResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FacultySubjectAssignmentResponse> listAssignments(Long facultyId) {
        return assignmentRepository.findByFacultyId(facultyId).stream()
                .map(FacultySubjectAssignmentResponse::from).collect(Collectors.toList());
    }
}
