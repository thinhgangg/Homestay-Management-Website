package com.homestay.homestayweb.service;

import com.homestay.homestayweb.dto.response.RoomImageResponse;
import com.homestay.homestayweb.entity.RoomImage;

import java.util.List;

public interface RoomImageService {
    void saveRoomImage(RoomImage roomImage);
    void uploadImageForRoom(Long roomId, String imageUrl);
    public List<RoomImageResponse> getRoomImagesByRoomId(Long roomId);
}
