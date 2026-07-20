package com.nabarangpur.erp.dto.attendance;

import com.nabarangpur.erp.entity.AttendanceRecord;
import com.nabarangpur.erp.entity.AttendanceSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class AttendanceSessionResponse {
    private Long sessionId;
    private Long classSectionId;
    private Long subjectId;
    private String subjectName;
    private LocalDate attendanceDate;
    private Integer sessionNumber;
    private boolean requiresApproval;
    private boolean approved;
    private List<RecordEntry> records;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecordEntry {
        private Long recordId;
        private Long studentId;
        private String studentName;
        private String studentCode;
        private String status;
        private String remarks;
    }

    public static AttendanceSessionResponse from(AttendanceSession s, List<AttendanceRecord> records) {
        return AttendanceSessionResponse.builder()
                .sessionId(s.getId())
                .classSectionId(s.getClassSection().getId())
                .subjectId(s.getSubject().getId())
                .subjectName(s.getSubject().getName())
                .attendanceDate(s.getAttendanceDate())
                .sessionNumber(s.getSessionNumber())
                .requiresApproval(s.isRequiresApproval())
                .approved(s.getApprovedAt() != null)
                .records(records.stream().map(r -> RecordEntry.builder()
                        .recordId(r.getId())
                        .studentId(r.getStudent().getId())
                        .studentName(r.getStudent().getFullName())
                        .studentCode(r.getStudent().getStudentCode())
                        .status(r.getStatus().name())
                        .remarks(r.getRemarks())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
