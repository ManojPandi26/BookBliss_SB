package com.BookBliss.Repository;

import com.BookBliss.Entity.OtpVerification;
import com.BookBliss.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByUserAndPurposeAndUsedFalseAndExpiresAtAfter(
            User user,
            OtpVerification.OtpPurpose purpose,
            LocalDateTime currentTime
    );

    @Query("SELECT o FROM OtpVerification o WHERE o.user = :user " +
            "AND o.otpCode = :otpCode " +
            "AND o.purpose = :purpose " +
            "AND o.used = false " +
            "AND o.expiresAt > :currentTime")
    Optional<OtpVerification> findValidOtp(
            User user,
            String otpCode,
            OtpVerification.OtpPurpose purpose,
            LocalDateTime currentTime
    );
}
