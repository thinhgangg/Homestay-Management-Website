package com.homestay.homestayweb.dto.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long homestayId;
    private Integer rating;
    private String comment;
}