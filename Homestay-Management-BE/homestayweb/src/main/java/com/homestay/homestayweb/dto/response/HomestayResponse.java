package com.homestay.homestayweb.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HomestayResponse {
    private Long id;
    private String name;
    private String street;
    private String ward;
    private String district;
    private String description;
    private Double surfRating;
    private String approveStatus;
    private Long approvedBy;
    private String contactInfo;
    private LocalDateTime createdAt;
}