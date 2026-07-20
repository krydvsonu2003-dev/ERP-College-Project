package com.nabarangpur.erp.dto.common;

import com.nabarangpur.erp.entity.MarkComponent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder @AllArgsConstructor
public class MarkComponentResponse {
    private Long id;
    private String code;
    private String name;
    private BigDecimal weightPercentage;

    public static MarkComponentResponse from(MarkComponent m) {
        return MarkComponentResponse.builder()
                .id(m.getId()).code(m.getCode()).name(m.getName()).weightPercentage(m.getWeightPercentage())
                .build();
    }
}
