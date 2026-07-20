package com.nabarangpur.erp.dto.attendance;

import com.nabarangpur.erp.entity.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MarkAttendanceRequest {
    @NotNull private Long classSectionId;
    @NotNull private Long subjectId;
    @NotNull private LocalDate attendanceDate;
    private Integer sessionNumber = 1;

    @NotEmpty
    @Valid
    private List<Entry> entries;

    @Getter
    @Setter
    public static class Entry {
        @NotNull private Long studentId;
        @NotNull private AttendanceStatus status;
        private String remarks;
    }
}
