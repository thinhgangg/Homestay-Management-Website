package com.homestay.homestayweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DailyRevenueResponse {
    private String day; // yyyy-MM-dd
    private BigDecimal totalRevenue;
}