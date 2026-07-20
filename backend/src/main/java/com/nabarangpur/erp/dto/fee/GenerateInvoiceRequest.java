package com.nabarangpur.erp.dto.fee;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GenerateInvoiceRequest {
    @NotNull private Long studentId;
    @NotNull private Long academicYearId;
    @NotNull private Integer semester;
    private LocalDate dueDate;
}
