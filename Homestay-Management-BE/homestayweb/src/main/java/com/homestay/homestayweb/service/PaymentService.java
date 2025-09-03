package com.homestay.homestayweb.service;

import com.homestay.homestayweb.dto.response.DailyRevenueResponse;
import com.homestay.homestayweb.dto.response.HomestayRevenueResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    boolean checkRoomPayment(Long roomId);
    String createVNPayPaymentUrl(Long bookingId, HttpServletRequest request) throws Exception;
    String handleVNPayReturn(Map<String, String> vnpParams);
    List<DailyRevenueResponse> getDailyRevenueByHost();
    List<HomestayRevenueResponse> getRevenueByHomestayByHost();
}
