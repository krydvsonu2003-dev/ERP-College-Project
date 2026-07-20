package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.attendance.*;
import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.service.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Functional Spec 6.3 - Attendance Management")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasAuthority('ATTENDANCE_CREATE')")
    public ApiResponse<AttendanceSessionResponse> mark(@Valid @RequestBody MarkAttendanceRequest request) {
        return ApiResponse.ok("Attendance recorded", attendanceService.markAttendance(request));
    }

    @PatchMapping("/records/{recordId}")
    @PreAuthorize("hasAuthority('ATTENDANCE_UPDATE')")
    public ApiResponse<Void> edit(@PathVariable Long recordId, @Valid @RequestBody EditAttendanceRequest request) {
        attendanceService.editAttendanceRecord(recordId, request);
        return ApiResponse.message("Attendance record updated");
    }

    @PostMapping("/sessions/{sessionId}/approve")
    @PreAuthorize("hasAuthority('ATTENDANCE_APPROVE')")
    public ApiResponse<Void> approve(@PathVariable Long sessionId) {
        attendanceService.approveSession(sessionId);
        return ApiResponse.message("Attendance session approved");
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    public ApiResponse<AttendanceSessionResponse> getSession(@PathVariable Long sessionId) {
        return ApiResponse.ok(attendanceService.getSession(sessionId));
    }

    @GetMapping("/my-sessions")
    @PreAuthorize("hasAuthority('ATTENDANCE_CREATE') or hasAuthority('ATTENDANCE_READ')")
    public ApiResponse<List<AttendanceSessionResponse>> mySessions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(attendanceService.facultySessionsForDate(date));
    }

    @GetMapping("/students/{studentId}/summary")
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    public ApiResponse<StudentAttendanceSummary> studentSummary(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(attendanceService.getStudentSummary(studentId, from, to));
    }
}
