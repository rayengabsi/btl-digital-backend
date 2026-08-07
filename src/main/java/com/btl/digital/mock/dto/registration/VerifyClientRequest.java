package com.btl.digital.mock.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyClientRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "ID type is required")
    @Pattern(regexp = "CIN|PASSPORT", message = "ID type must be CIN or PASSPORT")
    private String idType;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "\\d{8}", message = "Date of birth must have the format yyyyMMdd")
    private String dateOfBirth;

    @NotBlank(message = "Phone number is required")
    @Size(min = 8, max = 15, message = "Phone number must contain 8 to 15 digits")
    private String phoneNumber;
}