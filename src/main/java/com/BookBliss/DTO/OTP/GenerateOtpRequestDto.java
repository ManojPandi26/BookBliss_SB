package com.BookBliss.DTO.OTP;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateOtpRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP purpose is required")
    private String purpose; // Will be converted to OtpVerification.OtpPurpose
}
