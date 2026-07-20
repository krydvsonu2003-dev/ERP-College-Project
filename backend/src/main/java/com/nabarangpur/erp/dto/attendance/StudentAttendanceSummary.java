package com.nabarangpur.erp.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StudentAttendanceSummary {
    private Long studentId;
    private String studentName;
    private long totalSessions;
    private long present;
    private long absent;
    private long late;
    private long excused;
    private double attendancePercentage;
}
