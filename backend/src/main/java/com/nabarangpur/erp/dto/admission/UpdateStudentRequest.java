package com.nabarangpur.erp.dto.admission;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentRequest {
    @NotBlank private String fullName;
    private String mobileNumber;
    private String email;
    private String address;
    private String category;
}
