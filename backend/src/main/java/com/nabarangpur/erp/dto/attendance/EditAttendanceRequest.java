package com.nabarangpur.erp.dto.attendance;

import com.nabarangpur.erp.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditAttendanceRequest {
    @NotNull private AttendanceStatus newStatus;
    private String reason;
}
