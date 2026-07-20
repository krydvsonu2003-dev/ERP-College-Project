package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.admission.StudentResponse;
import com.nabarangpur.erp.dto.admission.UpdateStudentRequest;
import com.nabarangpur.erp.dto.admission.UpdateStudentStatusRequest;
import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.common.PageResponse;
import com.nabarangpur.erp.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Functional Spec 6.2 - student profile and lifecycle")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public ApiResponse<StudentResponse> myProfile() {
        return ApiResponse.ok(studentService.getMyProfile());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public ApiResponse<StudentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(studentService.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public ApiResponse<PageResponse<StudentResponse>> search(@RequestParam(required = false) String search,
                                                               @RequestParam(required = false) Long departmentId,
                                                               Pageable pageable) {
        return ApiResponse.ok(studentService.search(search, departmentId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return ApiResponse.ok("Student profile updated", studentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('STUDENT_APPROVE') or hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<StudentResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStudentStatusRequest request) {
        return ApiResponse.ok("Student status updated", studentService.updateStatus(id, request));
    }
}
