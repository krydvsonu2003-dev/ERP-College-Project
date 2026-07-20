package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.*;
import com.nabarangpur.erp.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboards", description = "Functional Spec 6.6 - role-based dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/principal")
    @PreAuthorize("hasRole('PRINCIPAL') or hasRole('SUPER_ADMIN') or hasRole('HOD')")
    public ApiResponse<PrincipalDashboardResponse> principal() {
        return ApiResponse.ok(dashboardService.principalDashboard());
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasRole('FACULTY')")
    public ApiResponse<FacultyDashboardResponse> faculty() {
        return ApiResponse.ok(dashboardService.facultyDashboard());
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<StudentDashboardResponse> student() {
        return ApiResponse.ok(dashboardService.studentDashboard());
    }

    @GetMapping("/accountant")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ApiResponse<AccountantDashboardResponse> accountant() {
        return ApiResponse.ok(dashboardService.accountantDashboard());
    }
}
