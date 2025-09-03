package com.homestay.homestayweb.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file);
    String uploadAndSave(MultipartFile file, Long homestayId, boolean isPrimary);
}
