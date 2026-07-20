package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.admission.StudentResponse;
import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.service.EnrollmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Class Enrollments", description = "Maps students to class sections for attendance/exam rosters")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<Void> enroll(@RequestBody Map<String, Object> body) {
        Long studentId = Long.valueOf(String.valueOf(body.get("studentId")));
        Long classSectionId = Long.valueOf(String.valueOf(body.get("classSectionId")));
        Integer semester = Integer.valueOf(String.valueOf(body.get("semester")));
        enrollmentService.enroll(studentId, classSectionId, semester);
        return ApiResponse.message("Student enrolled in class section");
    }

    @GetMapping("/roster")
    @PreAuthorize("hasAuthority('ATTENDANCE_CREATE') or hasAuthority('STUDENT_READ')")
    public ApiResponse<List<StudentResponse>> roster(@RequestParam Long classSectionId, @RequestParam Integer semester) {
        return ApiResponse.ok(enrollmentService.rosterFor(classSectionId, semester));
    }
}
