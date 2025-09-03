package com.homestay.homestayweb.service.implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.homestay.homestayweb.entity.Homestay;
import com.homestay.homestayweb.entity.HomestayImage;
import com.homestay.homestayweb.repository.HomestayImageRepository;
import com.homestay.homestayweb.service.CloudinaryService;
import com.homestay.homestayweb.service.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final HomestayService homestayService;  // Service để lấy Homestay theo ID
    private final HomestayImageRepository homestayImageRepository;  // Repository để lưu ảnh

    @Autowired
    public CloudinaryServiceImpl(Cloudinary cloudinary, HomestayService homestayService, HomestayImageRepository homestayImageRepository) {
        this.cloudinary = cloudinary;
        this.homestayService = homestayService;
        this.homestayImageRepository = homestayImageRepository;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh lên Cloudinary", e);
        }
    }

    @Override
    public String uploadAndSave(MultipartFile file, Long homestayId, boolean isPrimary) {
        // Upload ảnh lên Cloudinary
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString(); // Lấy URL ảnh

            // Tạo đối tượng HomestayImage
            Homestay homestay = homestayService.findEntityById(homestayId);  // Lấy homestay theo ID
            HomestayImage homestayImage = new HomestayImage();
            homestayImage.setImageUrl(imageUrl);
            homestayImage.setIsPrimary(isPrimary);  // Đánh dấu ảnh chính
            homestayImage.setHomestay(homestay);  // Gắn homestay

            // Lưu vào DB
            homestayImageRepository.save(homestayImage);

            return imageUrl; // Trả về URL ảnh đã upload
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload và lưu ảnh vào DB", e);
        }
    }
}
