package com.homestay.homestayweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomImageResponse {
    private Long imageId;
    private String imageUrl;
}
