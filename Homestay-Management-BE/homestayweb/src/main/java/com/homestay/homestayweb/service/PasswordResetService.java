package com.homestay.homestayweb.service;

public interface PasswordResetService {
    void sendOtp(String email);
    void resetPassword(String email, String newPassword, String otp);
}
