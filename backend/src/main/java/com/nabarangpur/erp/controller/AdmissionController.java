package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.admission.*;
import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.common.PageResponse;
import com.nabarangpur.erp.entity.AdmissionStatus;
import com.nabarangpur.erp.entity.AdmissionStatusHistory;
import com.nabarangpur.erp.service.AdmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admissions")
@RequiredArgsConstructor
@Tag(name = "Admissions", description = "Functional Spec 6.2 - admission workflow")
public class AdmissionController {

    private final AdmissionService admissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMISSION_CREATE')")
    public ApiResponse<AdmissionResponse> submit(@Valid @RequestBody SubmitAdmissionRequest request) {
        return ApiResponse.ok("Admission application submitted", admissionService.submit(request));
    }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('ADMISSION_CREATE') or hasAuthority('ADMISSION_UPDATE')")
    public ApiResponse<AdmissionService.AdmissionDocumentInfo> uploadDocument(
            @PathVariable Long id,
            @RequestParam String documentType,
            @RequestParam MultipartFile file) {
        return ApiResponse.ok("Document uploaded", admissionService.uploadDocument(id, documentType, file));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMISSION_READ')")
    public ApiResponse<AdmissionResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(admissionService.get(id));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('ADMISSION_READ')")
    public ApiResponse<List<AdmissionStatusHistory>> history(@PathVariable Long id) {
        return ApiResponse.ok(admissionService.history(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMISSION_READ')")
    public ApiResponse<PageResponse<AdmissionResponse>> list(@RequestParam(required = false) AdmissionStatus status,
                                                              Pageable pageable) {
        return ApiResponse.ok(admissionService.list(status, pageable));
    }

    @PatchMapping("/{id}/under-review")
    @PreAuthorize("hasAuthority('ADMISSION_UPDATE')")
    public ApiResponse<AdmissionResponse> markUnderReview(@PathVariable Long id,
                                                           @RequestBody VerifyAdmissionRequest request) {
        return ApiResponse.ok("Application moved to under review", admissionService.markUnderReview(id, request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ADMISSION_APPROVE')")
    public ApiResponse<StudentResponse> approve(@PathVariable Long id) {
        return ApiResponse.ok("Application approved and student record created", admissionService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ADMISSION_APPROVE')")
    public ApiResponse<AdmissionResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectAdmissionRequest request) {
        return ApiResponse.ok("Application rejected", admissionService.reject(id, request));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMISSION_DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        admissionService.deleteAdmission(id);
        return ApiResponse.ok("Admission deleted", null);
    }
}
