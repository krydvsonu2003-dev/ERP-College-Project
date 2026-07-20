package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.exam.*;
import com.nabarangpur.erp.entity.Examination;
import com.nabarangpur.erp.service.ExamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Tag(name = "Examinations & Results", description = "Functional Spec 6.4")
public class ExamController {

    private final ExamService examService;

    @PostMapping("/marks")
    @PreAuthorize("hasAuthority('EXAMINATION_CREATE')")
    public ApiResponse<ExamMarkResponse> enterMarks(@Valid @RequestBody EnterMarksRequest request) {
        return ApiResponse.ok("Marks recorded", examService.enterMarks(request));
    }

    @PostMapping("/marks/bulk")
    @PreAuthorize("hasAuthority('EXAMINATION_CREATE')")
    public ApiResponse<List<ExamMarkResponse>> bulkEnterMarks(@Valid @RequestBody BulkEnterMarksRequest request) {
        return ApiResponse.ok("Marks recorded for " + request.getEntries().size() + " students",
                examService.bulkEnterMarks(request));
    }

    @PostMapping("/compute-result")
    @PreAuthorize("hasAuthority('EXAMINATION_UPDATE')")
    public ApiResponse<ResultCardResponse> computeResult(@RequestParam Long examinationId,
                                                           @RequestParam Long subjectId,
                                                           @RequestParam Long studentId) {
        return ApiResponse.ok("Result computed", examService.computeResult(examinationId, subjectId, studentId));
    }

    @PostMapping("/result-cards/{resultCardId}/recalculate")
    @PreAuthorize("hasAuthority('EXAMINATION_APPROVE') or hasAuthority('EXAMINATION_UPDATE')")
    public ApiResponse<ResultCardResponse> recalculate(@PathVariable Long resultCardId,
                                                         @Valid @RequestBody RecalculateResultRequest request) {
        return ApiResponse.ok("Result recalculated", examService.recalculateResult(resultCardId, request));
    }

    @PostMapping("/{examinationId}/publish")
    @PreAuthorize("hasAuthority('EXAMINATION_PUBLISH')")
    public ApiResponse<Void> publish(@PathVariable Long examinationId, @RequestBody(required = false) Map<String, String> body) {
        examService.publishResults(examinationId, body == null ? null : body.get("remarks"));
        return ApiResponse.message("Results published");
    }

    @GetMapping("/{examinationId}/results")
    @PreAuthorize("hasAuthority('EXAMINATION_READ')")
    public ApiResponse<List<ResultCardResponse>> resultsForExamination(@PathVariable Long examinationId) {
        return ApiResponse.ok(examService.getResultsForExamination(examinationId));
    }

    @GetMapping("/students/{studentId}/results")
    @PreAuthorize("hasAuthority('EXAMINATION_READ')")
    public ApiResponse<List<ResultCardResponse>> resultsForStudent(@PathVariable Long studentId) {
        return ApiResponse.ok(examService.getResultsForStudent(studentId));
    }

    @GetMapping("/students/{studentId}/semester-results")
    @PreAuthorize("hasAuthority('EXAMINATION_READ')")
    public ApiResponse<List<SemesterResultResponse>> semesterResults(@PathVariable Long studentId) {
        return ApiResponse.ok(examService.getSemesterResults(studentId));
    }
    @GetMapping
    @PreAuthorize("hasAuthority('EXAMINATION_READ')")
    public ApiResponse<List<Examination>> getAllExaminations() {
        return ApiResponse.ok(examService.getAllExaminations());
    }
}
