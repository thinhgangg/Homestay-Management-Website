package com.homestay.homestayweb.dto.request;

import lombok.Data;

@Data
public class HomestayRequest {
    private String name;
    private String street;
    private String ward;
    private String district;
    private String description;
    private String approveStatus;
    private Long approvedBy;
    private String contactInfo;
}