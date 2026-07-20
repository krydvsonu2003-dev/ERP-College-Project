package com.nabarangpur.erp.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BulkEnterMarksRequest {
    @NotNull private Long examinationId;
    @NotNull private Long subjectId;
    @NotNull private Long componentId;
    @NotNull private BigDecimal maxMarks;

    @NotEmpty @Valid
    private List<Entry> entries;

    @Getter @Setter
    public static class Entry {
        @NotNull private Long studentId;
        @NotNull private BigDecimal marksObtained;
    }
}
