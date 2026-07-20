package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.exam.CreateExaminationRequest;
import com.nabarangpur.erp.dto.exam.ExaminationResponse;
import com.nabarangpur.erp.service.ExaminationSetupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examinations")
@RequiredArgsConstructor
@Tag(name = "Examination Setup", description = "Create/schedule examinations (precursor to marks entry)")
public class ExaminationSetupController {

    private final ExaminationSetupService examinationSetupService;

    @PostMapping
    @PreAuthorize("hasAuthority('EXAMINATION_CREATE')")
    public ApiResponse<ExaminationResponse> create(@Valid @RequestBody CreateExaminationRequest request) {
        return ApiResponse.ok("Examination created", examinationSetupService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EXAMINATION_READ')")
    public ApiResponse<List<ExaminationResponse>> list(@RequestParam(required = false) Long courseId,
                                                         @RequestParam(required = false) Integer semester) {
        return ApiResponse.ok(examinationSetupService.list(courseId, semester));
    }
}
