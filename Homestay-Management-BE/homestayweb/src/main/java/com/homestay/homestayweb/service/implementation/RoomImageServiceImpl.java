package com.homestay.homestayweb.service.implementation;

import com.homestay.homestayweb.dto.response.RoomImageResponse;
import com.homestay.homestayweb.entity.Room;
import com.homestay.homestayweb.entity.RoomImage;
import com.homestay.homestayweb.exception.ForbiddenException;
import com.homestay.homestayweb.exception.ResourceNotFoundException;
import com.homestay.homestayweb.repository.RoomImageRepository;
import com.homestay.homestayweb.repository.RoomRepository;
import com.homestay.homestayweb.security.UserDetailsImpl;
import com.homestay.homestayweb.service.RoomImageService;
import lombok.RequiredArgsConstructor;
import com.homestay.homestayweb.exception.BadRequestException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomImageServiceImpl implements RoomImageService {

    private final RoomImageRepository roomImageRepository;
    private final RoomRepository roomRepository;

    @Override
    public void saveRoomImage(RoomImage roomImage) {
        roomImageRepository.save(roomImage);
    }

    private void checkRoomOwnership(Room room) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long currentUserId = currentUser.getId();
        Long roomOwnerId = room.getHomestay().getHost().getId();

        if (!currentUserId.equals(roomOwnerId)) {
            throw new ForbiddenException("Bạn không có quyền thực hiện hành động này với phòng này.");
        }
    }

    public void uploadImageForRoom(Long roomId, String imageUrl) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Kiểm tra quyền sở hữu phòng
        checkRoomOwnership(room);

        // Kiểm tra nếu phòng đã có 2 ảnh
        List<RoomImage> existingImages = roomImageRepository.findByRoom_RoomId(roomId);
        if (existingImages.size() >= 2) {
            throw new BadRequestException("Mỗi phòng chỉ có tối đa 2 ảnh.");
        }

        RoomImage roomImage = new RoomImage();
        roomImage.setRoom(room);
        roomImage.setImageUrl(imageUrl);

        saveRoomImage(roomImage);
    }

    public List<RoomImageResponse> getRoomImagesByRoomId(Long roomId) {
        List<RoomImage> roomImages = roomImageRepository.findByRoom_RoomId(roomId);
        if (roomImages.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh cho phòng với ID: " + roomId);
        }

        return roomImages.stream()
                .map(roomImage -> new RoomImageResponse(roomImage.getImageId(), roomImage.getImageUrl()))
                .collect(Collectors.toList());
    }
}
