package com.BookBliss.Service.Auth;

import com.BookBliss.DTO.Auth.*;
import com.BookBliss.DTO.OTP.OtpVerificationDto;
import com.BookBliss.Entity.OtpVerification;
import jakarta.transaction.Transactional;

public interface AuthService {

    LoginResponseDto login(LoginRequestDto loginRequest);

    @Transactional
    void logout(String token);

    @Transactional
    UserDto registerNewUser(UserRegistrationDto registrationDto);

    boolean isAuthenticated();

    void initiatePasswordReset(ForgotPasswordDto.ForgotPasswordRequest request);

    void resetPassword(ForgotPasswordDto.ResetPasswordRequest request);

    // Refresh Token
    @org.springframework.transaction.annotation.Transactional
    TokenResponse refreshTokens(String refreshToken);

    //Otp related ....
    String generateOtp(String email, OtpVerification.OtpPurpose purpose);
    void sendOtpEmail(String email, OtpVerification.OtpPurpose purpose);
    boolean verifyOtp(OtpVerificationDto otpVerificationDto, OtpVerification.OtpPurpose purpose);


}
