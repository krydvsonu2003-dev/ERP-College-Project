package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.faculty.*;
import com.nabarangpur.erp.service.FacultyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
@Tag(name = "Faculty", description = "Faculty operational profiles and subject/class scoping")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping("/profiles")
    @PreAuthorize("hasAuthority('FACULTY_CREATE')")
    public ApiResponse<FacultyProfileResponse> createProfile(@Valid @RequestBody CreateFacultyProfileRequest request) {
        return ApiResponse.ok("Faculty profile created", facultyService.createProfile(request));
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAuthority('FACULTY_READ')")
    public ApiResponse<List<FacultyProfileResponse>> listProfiles() {
        return ApiResponse.ok(facultyService.listProfiles());
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAuthority('FACULTY_UPDATE')")
    public ApiResponse<FacultySubjectAssignmentResponse> assignSubject(@Valid @RequestBody AssignFacultySubjectRequest request) {
        return ApiResponse.ok("Subject assigned to faculty", facultyService.assignSubject(request));
    }

    @GetMapping("/{facultyId}/assignments")
    @PreAuthorize("hasAuthority('FACULTY_READ')")
    public ApiResponse<List<FacultySubjectAssignmentResponse>> listAssignments(@PathVariable Long facultyId) {
        return ApiResponse.ok(facultyService.listAssignments(facultyId));
    }
}
