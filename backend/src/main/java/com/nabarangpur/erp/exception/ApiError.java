package com.nabarangpur.erp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private boolean success;
    private String message;
    private int status;
    private String path;
    private Instant timestamp;
    private Map<String, String> fieldErrors;
}
