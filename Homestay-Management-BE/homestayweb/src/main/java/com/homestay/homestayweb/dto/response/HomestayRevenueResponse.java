package com.homestay.homestayweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class HomestayRevenueResponse {
    private String homestayName;
    private BigDecimal totalRevenue;
}
