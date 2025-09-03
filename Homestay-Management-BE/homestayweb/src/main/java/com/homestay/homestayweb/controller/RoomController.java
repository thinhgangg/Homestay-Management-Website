package com.homestay.homestayweb.controller;

import com.homestay.homestayweb.dto.request.RoomRequest;
import com.homestay.homestayweb.dto.response.HomestayResponse;
import com.homestay.homestayweb.dto.response.RoomImageResponse;
import com.homestay.homestayweb.dto.response.RoomResponse;
import com.homestay.homestayweb.entity.HomestayImage;
import com.homestay.homestayweb.entity.Room;
import com.homestay.homestayweb.entity.RoomImage;
import com.homestay.homestayweb.exception.BadRequestException;
import com.homestay.homestayweb.repository.RoomImageRepository;
import com.homestay.homestayweb.service.CloudinaryService;
import com.homestay.homestayweb.service.RoomImageService;
import com.homestay.homestayweb.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final CloudinaryService cloudinaryService;
    private final RoomService roomService;
    private final RoomImageService roomImageService;
    private final RoomImageRepository roomImageRepository;

    @PostMapping("/homestay/{homestayId}")
    @PreAuthorize("hasAuthority('CREATE_ROOM')")
    public ResponseEntity<RoomResponse> createRoom(@PathVariable Long homestayId,
                                                   @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(homestayId, request));
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("hasAuthority('UPDATE_ROOM')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasAuthority('DELETE_ROOM')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/valid-homestay/{homestayId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHomestay(@PathVariable Long homestayId) {
        return ResponseEntity.ok(roomService.getRoomsByHomestay(homestayId));
    }

    @GetMapping("/pending-homestay/{homestayId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHomestayP(@PathVariable Long homestayId) {
        return ResponseEntity.ok(roomService.getRoomsByHomestayP(homestayId,"PENDING"));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RoomResponse>> getAllPendingRooms(){
        return ResponseEntity.ok(roomService.getAllPendingRooms());
    }

    @GetMapping("/{roomId}")
    @PreAuthorize("hasAuthority('VIEW_ROOM')")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms(){
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PutMapping("/admin/pending/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public ResponseEntity<RoomResponse> pending(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.pendingRoom(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam Long homestayId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate
    ) {
        List<RoomResponse> availableRooms = roomService.getAvailableRooms(homestayId, checkInDate, checkOutDate);
        return ResponseEntity.ok(availableRooms);
    }

    @PostMapping("/{roomId}/images")
    @PreAuthorize("hasAuthority('CREATE_ROOM')")
    public ResponseEntity<String> uploadImageToRoom(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Upload ảnh lên Cloudinary và lấy URL
            String imageUrl = cloudinaryService.uploadFile(file);

            // Lưu ảnh cho phòng
            roomImageService.uploadImageForRoom(roomId, imageUrl);

            return ResponseEntity.ok(imageUrl); // Trả về URL ảnh đã upload
        } catch (BadRequestException e) {
            // Nếu đã có 2 ảnh, trả về lỗi 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Nếu có lỗi khác, trả về lỗi 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{roomId}/images")
    @PreAuthorize("hasAuthority('CREATE_ROOM')")
    public ResponseEntity<String> editImageToRoom(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Upload ảnh lên Cloudinary và lấy URL
            String imageUrl = cloudinaryService.uploadFile(file);

            List<RoomImage> images = roomImageRepository.findByRoom_RoomId(roomId);
            if (!images.isEmpty()) {
                roomImageRepository.deleteAll(images);
            }

            roomImageService.uploadImageForRoom(roomId, imageUrl);

            return ResponseEntity.ok(imageUrl); // Trả về URL ảnh đã upload
        } catch (BadRequestException e) {
            // Nếu đã có 2 ảnh, trả về lỗi 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Nếu có lỗi khác, trả về lỗi 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/{roomId}/images")
    public ResponseEntity<List<RoomImageResponse>> getRoomImagesByRoomId(@PathVariable Long roomId) {
        List<RoomImageResponse> roomImages = roomImageService.getRoomImagesByRoomId(roomId);
        return ResponseEntity.ok(roomImages); 
    }
}

