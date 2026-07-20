package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.exam.CreateExaminationRequest;
import com.nabarangpur.erp.dto.exam.ExaminationResponse;
import com.nabarangpur.erp.entity.AcademicYear;
import com.nabarangpur.erp.entity.Course;
import com.nabarangpur.erp.entity.Examination;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.AcademicYearRepository;
import com.nabarangpur.erp.repository.CourseRepository;
import com.nabarangpur.erp.repository.ExaminationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExaminationSetupService {

    private final ExaminationRepository examinationRepository;
    private final CourseRepository courseRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AuditService auditService;

    @Transactional
    public ExaminationResponse create(CreateExaminationRequest req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        AcademicYear year = academicYearRepository.findById(req.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        Examination exam = Examination.builder()
                .name(req.getName()).course(course).academicYear(year).semester(req.getSemester())
                .examType(req.getExamType()).startDate(req.getStartDate()).endDate(req.getEndDate())
                .build();
        exam = examinationRepository.save(exam);
        auditService.record("EXAMINATION_CREATE", "Examination", exam.getId(), null, req);
        return ExaminationResponse.from(exam);
    }

    @Transactional(readOnly = true)
    public List<ExaminationResponse> list(Long courseId, Integer semester) {
        List<Examination> exams = (courseId != null && semester != null)
                ? examinationRepository.findByCourseIdAndSemester(courseId, semester)
                : examinationRepository.findAll();
        return exams.stream().map(ExaminationResponse::from).collect(Collectors.toList());
    }
}
