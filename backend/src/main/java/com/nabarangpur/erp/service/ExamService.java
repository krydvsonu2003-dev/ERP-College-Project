package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.exam.*;
import com.nabarangpur.erp.entity.*;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.InvalidWorkflowStateException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.*;
import com.nabarangpur.erp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExaminationRepository examinationRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final ExamMarkRepository examMarkRepository;
    private final MarkComponentRepository markComponentRepository;
    private final ResultCardRepository resultCardRepository;
    private final ResultCardRevisionRepository revisionRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final ResultPublicationRepository resultPublicationRepository;
    private final GradeMasterRepository gradeMasterRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;
    private final AuditService auditService;

    @Transactional
    public ExamMarkResponse enterMarks(EnterMarksRequest req) {
        validateMarksRange(req.getMarksObtained(), req.getMaxMarks());
        enforceFacultyScopeOrPrivileged(req.getSubjectId());
        

        Examination examination = examinationRepository.findById(req.getExaminationId())
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found"));
        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        Student student = studentRepository.findByIdAndDeletedFalse(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        MarkComponent component = markComponentRepository.findById(req.getComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("Mark component not found"));

        User user = SecurityUtils.currentDomainUser();
        ExamMark mark = examMarkRepository
                .findByExaminationIdAndSubjectIdAndStudentIdAndComponentId(
                        examination.getId(), subject.getId(), student.getId(), component.getId())
                .orElse(ExamMark.builder()
                        .examination(examination).subject(subject).student(student).component(component)
                        .enteredBy(user)
                        .build());

        boolean isUpdate = mark.getId() != null;
        mark.setMarksObtained(req.getMarksObtained());
        mark.setMaxMarks(req.getMaxMarks());
        if (isUpdate) {
            mark.setUpdatedBy(user);
            mark.setUpdatedAt(java.time.Instant.now());
        }
        mark = examMarkRepository.save(mark);

        auditService.record(isUpdate ? "EXAM_MARKS_UPDATE" : "EXAM_MARKS_ENTER", "ExamMark", mark.getId(),
                null, req.getMarksObtained());

        return ExamMarkResponse.from(mark);
    }

    @Transactional
    public List<ExamMarkResponse> bulkEnterMarks(BulkEnterMarksRequest req) {
        return req.getEntries().stream().map(entry -> {
            EnterMarksRequest single = new EnterMarksRequest();
            single.setExaminationId(req.getExaminationId());
            single.setSubjectId(req.getSubjectId());
            single.setComponentId(req.getComponentId());
            single.setStudentId(entry.getStudentId());
            single.setMarksObtained(entry.getMarksObtained());
            single.setMaxMarks(req.getMaxMarks());
            return enterMarks(single);
        }).collect(Collectors.toList());
    }

    /** Computes the final per-subject result from all entered mark components. */
    @Transactional
    public ResultCardResponse computeResult(Long examinationId, Long subjectId, Long studentId) {
        return computeResultInternal(examinationId, subjectId, studentId, null);
    }

    @Transactional
    public ResultCardResponse recalculateResult(Long resultCardId, RecalculateResultRequest req) {
        ResultCard existing = resultCardRepository.findById(resultCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Result card not found"));

        if (existing.getStatus() == ResultStatus.PUBLISHED && !SecurityUtils.isPrivileged()) {
            throw new BadRequestException("Recalculating a published result requires Principal/HOD/Admin authorization");
        }

        BigDecimal previousMarks = existing.getTotalMarksObtained();
        String previousGrade = existing.getGrade() != null ? existing.getGrade().getGradeLetter() : null;

        ResultCardResponse recomputed = computeResultInternal(
                existing.getExamination().getId(), existing.getSubject().getId(), existing.getStudent().getId(),
                existing.getStatus());

        // Any post-publication correction must be versioned and auditable.
        revisionRepository.save(ResultCardRevision.builder()
                .resultCard(existing)
                .previousMarks(previousMarks)
                .previousGrade(previousGrade)
                .reason(req.getReason())
                .revisedBy(SecurityUtils.currentDomainUser())
                .build());

        auditService.record("EXAM_RESULT_RECALCULATE", "ResultCard", existing.getId(), previousMarks, recomputed);
        return recomputed;
    }

    private ResultCardResponse computeResultInternal(Long examinationId, Long subjectId, Long studentId,
                                                       ResultStatus preserveStatusIfPublished) {
        List<ExamMark> marks = examMarkRepository.findByExaminationIdAndSubjectIdAndStudentId(examinationId, subjectId, studentId);
        if (marks.isEmpty()) {
            throw new BadRequestException("No marks entered yet for this student/subject/examination");
        }

        BigDecimal totalObtained = marks.stream().map(ExamMark::getMarksObtained).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMax = marks.stream().map(ExamMark::getMaxMarks).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal percentage = totalMax.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalObtained.multiply(BigDecimal.valueOf(100)).divide(totalMax, 2, RoundingMode.HALF_UP);

        
        GradeMaster grade = gradeMasterRepository.findByPercentage(percentage).orElse(null);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Student student = studentRepository.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Examination examination = examinationRepository.findById(examinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found"));

        ResultCard card = resultCardRepository.findByStudentIdAndExaminationIdAndSubjectId(studentId, examinationId, subjectId)
                .orElse(ResultCard.builder().student(student).examination(examination).subject(subject)
                        .credits(subject.getCredits()).build());

        card.setTotalMarksObtained(totalObtained);
        card.setTotalMaxMarks(totalMax);
        card.setPercentage(percentage);
        card.setGrade(grade);
        card.setGradePoint(grade != null ? grade.getGradePoint() : BigDecimal.ZERO);
        card.setCredits(subject.getCredits());
        if (preserveStatusIfPublished == null) {
            card.setStatus(ResultStatus.DRAFT);
        } else {
            card.setVersion(card.getVersion() + 1);
        }
        card = resultCardRepository.save(card);

        return ResultCardResponse.from(card);
    }

    /** Publish all DRAFT result cards for an examination at once. */
    @Transactional
    public void publishResults(Long examinationId, String remarks) {
        Examination examination = examinationRepository.findById(examinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found"));

        List<ResultCard> cards = resultCardRepository.findByExaminationId(examinationId);
        if (cards.isEmpty()) {
            throw new InvalidWorkflowStateException("No computed results to publish for this examination");
        }
        cards.forEach(c -> c.setStatus(ResultStatus.PUBLISHED));
        resultCardRepository.saveAll(cards);

        examination.setStatus(ExaminationStatus.PUBLISHED);
        examinationRepository.save(examination);

        resultPublicationRepository.save(ResultPublication.builder()
                .examination(examination)
                .publishedBy(SecurityUtils.currentDomainUser())
                .publishedAt(Instant.now())
                .remarks(remarks)
                .build());
        System.out.println("Total Result Cards = " + cards.size());

        cards.forEach(c ->
            System.out.println(
                "Student = " + c.getStudent().getId() +
                " Status = " + c.getStatus()
            )
        );
        // Recompute SGPA/CGPA for every student who has a result in this examination.
        cards.stream().map(c -> c.getStudent().getId()).distinct()
                .forEach(studentId -> computeSemesterResult(studentId, examination));

        auditService.record("EXAM_RESULT_PUBLISH", "Examination", examinationId, null,
                "Published " + cards.size() + " result cards");
    }

    private void computeSemesterResult(Long studentId, Examination examination) {
    	System.out.println("=================================");
        System.out.println("computeSemesterResult() called");
        System.out.println("Student Id = " + studentId);
        System.out.println("Exam Id = " + examination.getId());
        List<ResultCard> cards = resultCardRepository.findByStudentIdAndExaminationId(studentId, examination.getId());
        System.out.println("Cards Found = " + cards.size());
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal creditSum = BigDecimal.ZERO;
        for (ResultCard c : cards) {
            if (c.getGradePoint() != null && c.getCredits() != null) {
                weightedSum = weightedSum.add(c.getGradePoint().multiply(c.getCredits()));
                creditSum = creditSum.add(c.getCredits());
            }
        }
        BigDecimal sgpa = creditSum.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : weightedSum.divide(creditSum, 2, RoundingMode.HALF_UP);

        // CGPA: credit-weighted average of all semesters completed so far (including this one).
        List<SemesterResult> previous = semesterResultRepository.findByStudentIdOrderBySemesterAsc(studentId);
        BigDecimal cumulativeWeighted = weightedSum;
        BigDecimal cumulativeCredits = creditSum;
        for (SemesterResult sr : previous) {
            if (sr.getSemester().equals(examination.getSemester())) continue; // avoid double counting this semester
            if (sr.getSgpa() != null && sr.getTotalCredits() != null) {
                cumulativeWeighted = cumulativeWeighted.add(sr.getSgpa().multiply(sr.getTotalCredits()));
                cumulativeCredits = cumulativeCredits.add(sr.getTotalCredits());
            }
        }
        BigDecimal cgpa = cumulativeCredits.compareTo(BigDecimal.ZERO) == 0
                ? sgpa
                : cumulativeWeighted.divide(cumulativeCredits, 2, RoundingMode.HALF_UP);

        SemesterResult semesterResult = semesterResultRepository
                .findByStudentIdAndAcademicYearIdAndSemester(studentId, examination.getAcademicYear().getId(), examination.getSemester())
                .orElse(SemesterResult.builder()
                        .student(cards.get(0).getStudent())
                        .academicYear(examination.getAcademicYear())
                        .semester(examination.getSemester())
                        .computedAt(Instant.now()) 
                        .build());

        semesterResult.setSgpa(sgpa);
        semesterResult.setCgpa(cgpa);
        semesterResult.setTotalCredits(creditSum);
        semesterResult.setStatus(ResultStatus.PUBLISHED);
        semesterResultRepository.save(semesterResult);
        System.out.println("Saving Semester Result");
        System.out.println("SGPA = " + sgpa);
        System.out.println("CGPA = " + cgpa);
        System.out.println("Credits = " + creditSum);

        semesterResult = semesterResultRepository.save(semesterResult);

        System.out.println("Saved ID = " + semesterResult.getId());
    }

    @Transactional(readOnly = true)
    public List<ResultCardResponse> getResultsForExamination(Long examinationId) {
        return resultCardRepository.findByExaminationId(examinationId).stream()
                .map(ResultCardResponse::from).collect(Collectors.toList());
    }

    /** Results should not be visible to students until published (6.4.4). */
    @Transactional(readOnly = true)
    public List<ResultCardResponse> getResultsForStudent(Long studentId) {
        enforceSelfAccessIfStudent(studentId);
        List<ResultCard> cards = SecurityUtils.isStudent()
                ? resultCardRepository.findByStudentIdAndStatus(studentId, ResultStatus.PUBLISHED)
                : resultCardRepository.findByStudentIdAndStatus(studentId, ResultStatus.PUBLISHED);
        return cards.stream().map(ResultCardResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SemesterResultResponse> getSemesterResults(Long studentId) {
        enforceSelfAccessIfStudent(studentId);
        return semesterResultRepository.findByStudentIdOrderBySemesterAsc(studentId).stream()
                .filter(r -> !SecurityUtils.isStudent() || r.getStatus() == ResultStatus.PUBLISHED)
                .map(SemesterResultResponse::from).collect(Collectors.toList());
    }

    private void enforceSelfAccessIfStudent(Long studentId) {
        if (SecurityUtils.isStudent()) {
        	Student student = studentRepository.findByIdAndDeletedFalse(studentId)
        	        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            Long currentUserId = SecurityUtils.currentUserId();
            if (student.getUser() == null || !student.getUser().getId().equals(currentUserId)) {
                throw new BadRequestException("Students may only view their own results");
            }
        }
    }

    private void validateMarksRange(BigDecimal obtained, BigDecimal max) {
        if (obtained.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Marks obtained cannot be negative");
        }
        if (obtained.compareTo(max) > 0) {
            throw new BadRequestException("Marks obtained cannot exceed maximum marks");
        }
    }

    private void enforceFacultyScopeOrPrivileged(Long subjectId) {
        if (SecurityUtils.isPrivileged()) return;
        if (!SecurityUtils.isFaculty()) return; // accountants etc. don't enter marks anyway (no privilege)
        Optional<FacultyProfile> faculty = facultyProfileRepository.findByUserId(SecurityUtils.currentUserId());
        if (faculty.isEmpty()) {
            throw new BadRequestException("Current user has no faculty profile");
        }
        boolean assigned = assignmentRepository.findByFacultyId(faculty.get().getId()).stream()
                .anyMatch(a -> a.getSubject().getId().equals(subjectId));
        if (!assigned) {
            throw new BadRequestException("You are not assigned to teach this subject");
        }
    }
    @Transactional(readOnly = true)
    public List<Examination> getAllExaminations() {
        return examinationRepository.findAll();
    }
}
