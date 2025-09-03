package com.homestay.homestayweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private Long bookingId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long totalPrice;
    private Long userId;
    private Long roomId;
    private LocalDate createdAt;

    private String userEmail;
    private Long homestayId;
    private String homestayName;
}
