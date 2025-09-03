package com.homestay.homestayweb.service.implementation;

import com.homestay.homestayweb.dto.response.DailyRevenueResponse;
import com.homestay.homestayweb.dto.response.HomestayRevenueResponse;
import com.homestay.homestayweb.entity.Booking;
import com.homestay.homestayweb.entity.Payment;
import com.homestay.homestayweb.entity.User;
import com.homestay.homestayweb.repository.BookingRepository;
import com.homestay.homestayweb.repository.PaymentRepository;
import com.homestay.homestayweb.repository.UserRepository;
import com.homestay.homestayweb.security.UserDetailsImpl;
import com.homestay.homestayweb.service.BookingService;
import com.homestay.homestayweb.service.PaymentService;
import com.homestay.homestayweb.utils.VnPayUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    private String vnp_Url;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Override
    public boolean checkRoomPayment(Long roomId) {
        return paymentRepository.existsByBooking_BookingId(roomId);
    }

    @Override
    public String createVNPayPaymentUrl(Long bookingId, HttpServletRequest request) throws Exception {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_OrderInfo = "Thanh toan don dat phong #" + bookingId;
        String orderType = "other";
        String amount = String.valueOf((long) (booking.getTotalPrice() * 100));

        String vnp_IpAddr = request.getRemoteAddr();
        String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        return VnPayUtil.getPaymentUrl(vnp_Params, vnp_HashSecret, vnp_Url);
    }

    @Override
    @Transactional
    public String handleVNPayReturn(Map<String, String> vnpParams) {
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String bookingIdRaw = vnpParams.get("vnp_OrderInfo").replaceAll("[^0-9]", "");

        if ("00".equals(responseCode)) {
            Long bookingId = Long.parseLong(bookingIdRaw);

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            bookingService.pendingBooking(bookingId);
            User user = booking.getUser();

            Payment payment = Payment.builder()
                    .booking(booking)
                    .user(booking.getUser())
                    .amount(Double.parseDouble(vnpParams.get("vnp_Amount")) / 100)
                    .paymentMethod("VNPay")
                    .paymentStatus("Completed")
                    .createdAt(new Date())
                    .build();

            paymentRepository.save(payment);
            return "Thanh toán thành công cho đơn #" + bookingId;

        }
        return "Thanh toán thất bại hoặc bị hủy";
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetailsImpl) principal).getEmail();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("User not authenticated");
    }

    @Override
    public List<DailyRevenueResponse> getDailyRevenueByHost() {
        User currentUser = getCurrentUser(); // User hiện tại đăng nhập (host)
        List<Object[]> results = paymentRepository.getDailyRevenueByHost(currentUser.getId());

        List<DailyRevenueResponse> responseList = new ArrayList<>();
        for (Object[] row : results) {
            String day = row[0].toString();
            Object value = row[1];
            BigDecimal totalRevenue;
            if (value instanceof BigDecimal) {
                totalRevenue = (BigDecimal) value;
            } else if (value instanceof Double) {
                totalRevenue = BigDecimal.valueOf((Double) value);
            } else if (value instanceof Number) {
                totalRevenue = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                totalRevenue = BigDecimal.ZERO;
            }
            responseList.add(new DailyRevenueResponse(day, totalRevenue));
        }
        return responseList;
    }

    @Override
    public List<HomestayRevenueResponse> getRevenueByHomestayByHost() {
        User currentUser = getCurrentUser();
        List<Object[]> results = paymentRepository.getRevenueByHomestayByHost(currentUser.getId());

        List<HomestayRevenueResponse> responseList = new ArrayList<>();
        for (Object[] row : results) {
            String homestayName = (String) row[0];
            Object value = row[1];
            BigDecimal totalRevenue;
            if (value instanceof BigDecimal) {
                totalRevenue = (BigDecimal) value;
            } else if (value instanceof Double) {
                totalRevenue = BigDecimal.valueOf((Double) value);
            } else if (value instanceof Number) {
                totalRevenue = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                totalRevenue = BigDecimal.ZERO;
            }
            responseList.add(new HomestayRevenueResponse(homestayName, totalRevenue));
        }
        return responseList;
    }
}
