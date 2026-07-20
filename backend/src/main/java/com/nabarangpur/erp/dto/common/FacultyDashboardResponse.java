package com.nabarangpur.erp.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FacultyDashboardResponse {
    private long assignedClassCount;
    private List<AssignmentInfo> assignments;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AssignmentInfo {
        private Long subjectId;
        private String subjectName;
        private Long classSectionId;
        private Integer semester;
    }
}
