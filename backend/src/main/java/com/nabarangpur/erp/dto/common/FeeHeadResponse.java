package com.nabarangpur.erp.dto.common;

import com.nabarangpur.erp.entity.FeeHead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class FeeHeadResponse {
    private Long id;
    private String name;
    private String code;
    private String description;

    public static FeeHeadResponse from(FeeHead f) {
        return FeeHeadResponse.builder().id(f.getId()).name(f.getName()).code(f.getCode())
                .description(f.getDescription()).build();
    }
}
