package com.nabarangpur.erp.dto.admission;

import com.nabarangpur.erp.entity.StudentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentStatusRequest {
    @NotNull private StudentStatus status;
    private String remarks;
}
