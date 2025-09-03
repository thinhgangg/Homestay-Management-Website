package com.homestay.homestayweb.controller;

import com.homestay.homestayweb.dto.request.*;
import com.homestay.homestayweb.dto.response.JwtResponse;
import com.homestay.homestayweb.service.AuthService;
import com.homestay.homestayweb.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public String register(@RequestBody SignupRequest request) {
        return authService.register(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(@RequestBody EmailRequest req) {
        passwordResetService.sendOtp(req.getEmail());
        return ResponseEntity.ok("Đã gửi OTP về email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.getEmail(), req.getNewPassword(), req.getOtp());
        return ResponseEntity.ok("Đặt lại mật khẩu thành công.");
    }
}
