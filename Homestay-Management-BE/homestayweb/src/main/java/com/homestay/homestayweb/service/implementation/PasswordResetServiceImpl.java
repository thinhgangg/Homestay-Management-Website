package com.homestay.homestayweb.service.implementation;

import com.homestay.homestayweb.entity.PasswordResetToken;
import com.homestay.homestayweb.entity.User;
import com.homestay.homestayweb.repository.PasswordResetTokenRepository;
import com.homestay.homestayweb.repository.UserRepository;
import com.homestay.homestayweb.service.PasswordResetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public void sendOtp(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // OTP 6 số
        tokenRepo.deleteByEmail(email); // Xóa OTP cũ nếu có

        tokenRepo.save(PasswordResetToken.builder()
                .email(email)
                .otp(otp)
                .expirationTime(LocalDateTime.now().plusMinutes(5))
                .build());

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Mã OTP đặt lại mật khẩu");
        msg.setText("Mã OTP của bạn là: " + otp);
        mailSender.send(msg);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword, String otp) {
        PasswordResetToken token = tokenRepo.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new RuntimeException("OTP không đúng hoặc đã hết hạn"));

        if (token.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.deleteByEmail(email); // cleanup
    }
}
