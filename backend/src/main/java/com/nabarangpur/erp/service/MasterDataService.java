package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.common.*;
import com.nabarangpur.erp.entity.*;
import com.nabarangpur.erp.exception.ConflictException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Functional Spec section 7 - Master Data Requirements.
 * Centralized CRUD for departments, courses, academic years, subjects,
 * class sections and fee heads, used across every other module.
 */
@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSectionRepository classSectionRepository;
    private final FeeHeadRepository feeHeadRepository;
    private final MarkComponentRepository markComponentRepository;
    private final AuditService auditService;

    // ---- Departments ----
    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        return departmentRepository.findAll().stream().map(DepartmentResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest req) {
        Department d = Department.builder().name(req.getName()).code(req.getCode()).build();
        d = departmentRepository.save(d);
        auditService.record("DEPARTMENT_CREATE", "Department", d.getId(), null, req);
        return DepartmentResponse.from(d);
    }

    // ---- Courses ----
    @Transactional(readOnly = true)
    public List<CourseResponse> listCourses() {
        return courseRepository.findAll().stream().map(CourseResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest req) {
        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Course c = Course.builder()
                .department(dept).name(req.getName()).code(req.getCode())
                .durationYears(req.getDurationYears()).totalSemesters(req.getTotalSemesters())
                .build();
        c = courseRepository.save(c);
        auditService.record("COURSE_CREATE", "Course", c.getId(), null, req);
        return CourseResponse.from(c);
    }

    // ---- Academic Years ----
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> listAcademicYears() {
        return academicYearRepository.findAll().stream().map(AcademicYearResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicYear getCurrentAcademicYearOrThrow() {
        return academicYearRepository.findByCurrentTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No current academic year configured"));
    }

    // ---- Subjects ----
    @Transactional(readOnly = true)
    public List<SubjectResponse> listSubjects(Long courseId, Integer semester) {
        List<Subject> subjects = (courseId != null && semester != null)
                ? subjectRepository.findByCourseIdAndSemester(courseId, semester)
                : subjectRepository.findAll();
        return subjects.stream().map(SubjectResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Subject s = Subject.builder()
                .course(course).semester(req.getSemester()).name(req.getName()).code(req.getCode())
                .credits(req.getCredits() != null ? req.getCredits() : BigDecimal.valueOf(4))
                .build();
        s = subjectRepository.save(s);
        auditService.record("SUBJECT_CREATE", "Subject", s.getId(), null, req);
        return SubjectResponse.from(s);
    }

    // ---- Class Sections ----
    @Transactional(readOnly = true)
    public List<ClassSectionResponse> listClassSections(Long courseId, Long academicYearId) {
        List<ClassSection> sections = (courseId != null && academicYearId != null)
                ? classSectionRepository.findByCourseIdAndAcademicYearId(courseId, academicYearId)
                : classSectionRepository.findAll();
        return sections.stream().map(ClassSectionResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ClassSectionResponse createClassSection(CreateClassSectionRequest req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        AcademicYear year = academicYearRepository.findById(req.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));
        ClassSection cs = ClassSection.builder()
                .course(course).academicYear(year).semester(req.getSemester())
                .sectionName(req.getSectionName() == null ? "A" : req.getSectionName())
                .build();
        cs = classSectionRepository.save(cs);
        auditService.record("CLASS_SECTION_CREATE", "ClassSection", cs.getId(), null, req);
        return ClassSectionResponse.from(cs);
    }

    // ---- Fee Heads ----
    @Transactional(readOnly = true)
    public List<FeeHeadResponse> listFeeHeads() {
        return feeHeadRepository.findAll().stream().map(FeeHeadResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public FeeHeadResponse createFeeHead(CreateFeeHeadRequest req) {
        FeeHead f = FeeHead.builder().name(req.getName()).code(req.getCode()).description(req.getDescription()).build();
        f = feeHeadRepository.save(f);
        auditService.record("FEE_HEAD_CREATE", "FeeHead", f.getId(), null, req);
        return FeeHeadResponse.from(f);
    }

    // ---- Mark Components ----
    @Transactional(readOnly = true)
    public List<MarkComponentResponse> listMarkComponents() {
        return markComponentRepository.findAll().stream().map(MarkComponentResponse::from).collect(Collectors.toList());
    }
}
