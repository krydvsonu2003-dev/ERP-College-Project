package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.*;
import com.nabarangpur.erp.service.MasterDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
@Tag(name = "Master Data", description = "Functional Spec 7 - departments, courses, academic years, subjects, fee heads")
public class MasterDataController {

    private final MasterDataService masterDataService;

    @GetMapping("/departments")
    public ApiResponse<List<DepartmentResponse>> departments() {
        return ApiResponse.ok(masterDataService.listDepartments());
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest req) {
        return ApiResponse.ok("Department created", masterDataService.createDepartment(req));
    }

    @GetMapping("/courses")
    public ApiResponse<List<CourseResponse>> courses() {
        return ApiResponse.ok(masterDataService.listCourses());
    }

    @PostMapping("/courses")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest req) {
        return ApiResponse.ok("Course created", masterDataService.createCourse(req));
    }

    @GetMapping("/academic-years")
    public ApiResponse<List<AcademicYearResponse>> academicYears() {
        return ApiResponse.ok(masterDataService.listAcademicYears());
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectResponse>> subjects(@RequestParam(required = false) Long courseId,
                                                         @RequestParam(required = false) Integer semester) {
        return ApiResponse.ok(masterDataService.listSubjects(courseId, semester));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    public ApiResponse<SubjectResponse> createSubject(@Valid @RequestBody CreateSubjectRequest req) {
        return ApiResponse.ok("Subject created", masterDataService.createSubject(req));
    }

    @GetMapping("/class-sections")
    public ApiResponse<List<ClassSectionResponse>> classSections(@RequestParam(required = false) Long courseId,
                                                                   @RequestParam(required = false) Long academicYearId) {
        return ApiResponse.ok(masterDataService.listClassSections(courseId, academicYearId));
    }

    @PostMapping("/class-sections")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    public ApiResponse<ClassSectionResponse> createClassSection(@Valid @RequestBody CreateClassSectionRequest req) {
        return ApiResponse.ok("Class section created", masterDataService.createClassSection(req));
    }

    @GetMapping("/fee-heads")
    public ApiResponse<List<FeeHeadResponse>> feeHeads() {
        return ApiResponse.ok(masterDataService.listFeeHeads());
    }

    @PostMapping("/fee-heads")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    public ApiResponse<FeeHeadResponse> createFeeHead(@Valid @RequestBody CreateFeeHeadRequest req) {
        return ApiResponse.ok("Fee head created", masterDataService.createFeeHead(req));
    }

    @GetMapping("/mark-components")
    public ApiResponse<List<MarkComponentResponse>> markComponents() {
        return ApiResponse.ok(masterDataService.listMarkComponents());
    }
}
