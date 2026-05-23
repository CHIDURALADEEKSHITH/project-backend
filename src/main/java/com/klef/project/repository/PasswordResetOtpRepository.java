package com.klef.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.klef.project.entity.PasswordResetOtp;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Integer>
{
    PasswordResetOtp findByEmailAndOtp(String email, String otp);
    PasswordResetOtp findByEmail(String email);
}