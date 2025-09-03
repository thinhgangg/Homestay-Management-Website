package com.homestay.homestayweb.service.implementation;
import com.homestay.homestayweb.dto.response.HomestayImageResponse;
import com.homestay.homestayweb.entity.Homestay;
import com.homestay.homestayweb.entity.HomestayImage;
import com.homestay.homestayweb.exception.ForbiddenException;
import com.homestay.homestayweb.exception.ResourceNotFoundException;
import com.homestay.homestayweb.repository.HomestayImageRepository;
import com.homestay.homestayweb.repository.HomestayRepository;
import com.homestay.homestayweb.security.UserDetailsImpl;
import com.homestay.homestayweb.service.HomestayImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomestayImageServiceImpl implements HomestayImageService {

    private final HomestayImageRepository homestayImageRepository;
    private final HomestayRepository homestayRepository;

    @Override
    public HomestayImage getPrimaryImage(Homestay homestay) {
        // Tìm ảnh chính của homestay
        return homestayImageRepository.findByHomestayAndIsPrimaryTrue(homestay);
    }

    @Override
    public void saveHomestayImage(HomestayImage homestayImage) {
        // Lưu ảnh vào DB
        homestayImageRepository.save(homestayImage);
    }

    private void checkHomestayOwnership(Homestay homestay) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long currentUserId = currentUser.getId();
        Long homestayOwnerId = homestay.getHost().getId();

        if (!currentUserId.equals(homestayOwnerId)) {
            throw new ForbiddenException("Bạn không có quyền thực hiện hành động này với homestay này.");
        }
    }

    // Phương thức upload ảnh cho homestay, bao gồm kiểm tra quyền sở hữu homestay
    public void uploadImageForHomestay(Long homestayId, String imageUrl) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));

        // Kiểm tra quyền sở hữu homestay
        checkHomestayOwnership(homestay);

        HomestayImage homestayImage = new HomestayImage();
        homestayImage.setHomestay(homestay);
        homestayImage.setImageUrl(imageUrl);

        saveHomestayImage(homestayImage);
    }

    public List<HomestayImageResponse> getHomestayImageByHomestayId(Long homestayId) {
        List<HomestayImage> homestayImages = homestayImageRepository.findByHomestay_HomestayId(homestayId);
        if (homestayImages.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh của Homestay với ID: " + homestayId);
        }

        return homestayImages.stream()
                .map(homestayImage -> new HomestayImageResponse(homestayImage.getImageId(), homestayImage.getImageUrl()))
                .collect(Collectors.toList());
    }

}
