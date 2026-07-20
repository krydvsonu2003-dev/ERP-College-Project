package com.nabarangpur.erp.dto.common;

import com.nabarangpur.erp.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder @AllArgsConstructor
public class SubjectResponse {
    private Long id;
    private String name;
    private String code;
    private Long courseId;
    private Integer semester;
    private BigDecimal credits;

    public static SubjectResponse from(Subject s) {
        return SubjectResponse.builder()
                .id(s.getId()).name(s.getName()).code(s.getCode())
                .courseId(s.getCourse().getId()).semester(s.getSemester())
                .credits(s.getCredits())
                .build();
    }
}
