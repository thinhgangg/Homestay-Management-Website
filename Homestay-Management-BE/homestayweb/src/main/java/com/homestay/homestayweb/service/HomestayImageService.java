package com.homestay.homestayweb.service;

import com.homestay.homestayweb.dto.response.HomestayImageResponse;
import com.homestay.homestayweb.entity.Homestay;
import com.homestay.homestayweb.entity.HomestayImage;

import java.util.List;

public interface HomestayImageService {
    HomestayImage getPrimaryImage(Homestay homestay);
    void saveHomestayImage(HomestayImage homestayImage);
    void uploadImageForHomestay(Long homestayId, String imageURL);
    List<HomestayImageResponse> getHomestayImageByHomestayId(Long homestayId);
}