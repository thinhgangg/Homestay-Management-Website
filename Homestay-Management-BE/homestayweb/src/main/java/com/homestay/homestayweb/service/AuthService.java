package com.homestay.homestayweb.service;

import com.homestay.homestayweb.dto.request.LoginRequest;
import com.homestay.homestayweb.dto.request.SignupRequest;
import com.homestay.homestayweb.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    String register(SignupRequest request);
}