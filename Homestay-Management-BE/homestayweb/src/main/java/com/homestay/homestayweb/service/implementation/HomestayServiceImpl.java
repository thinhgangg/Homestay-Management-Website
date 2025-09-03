package com.homestay.homestayweb.service.implementation;

import com.homestay.homestayweb.dto.request.HomestayRequest;
import com.homestay.homestayweb.dto.response.BookingResponse;
import com.homestay.homestayweb.dto.response.HomestayResponse;
import com.homestay.homestayweb.entity.Homestay;
import com.homestay.homestayweb.entity.HomestayImage;
import com.homestay.homestayweb.entity.Room;
import com.homestay.homestayweb.entity.User;
import com.homestay.homestayweb.exception.BadRequestException;
import com.homestay.homestayweb.exception.DuplicateResourceException;
import com.homestay.homestayweb.exception.ForbiddenException;
import com.homestay.homestayweb.exception.ResourceNotFoundException;
import com.homestay.homestayweb.repository.HomestayImageRepository;
import com.homestay.homestayweb.repository.HomestayRepository;
import com.homestay.homestayweb.repository.RoomRepository;
import com.homestay.homestayweb.repository.UserRepository;
import com.homestay.homestayweb.security.UserDetailsImpl;
import com.homestay.homestayweb.service.HomestayService;
import com.homestay.homestayweb.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomestayServiceImpl implements HomestayService {
    private final HomestayRepository homestayRepository;
    private final UserRepository userRepository;
    private final HomestayImageRepository homestayImageRepository;
    private final RoomService roomService;
    private final RoomRepository roomRepository;

    @Override
    public HomestayResponse createHomestay(HomestayRequest request) {
        // Kiểm tra tên
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Tên homestay không được để trống.");
        }

        // Kiểm tra định dạng email
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.matches(emailRegex, request.getContactInfo())) {
            throw new BadRequestException("Email không đúng định dạng.");
        }

        // Kiểm tra email trùng
        if (homestayRepository.existsByContactInfo(request.getContactInfo())) {
            throw new DuplicateResourceException("Email đã tồn tại.");
        }

        // Kiểm tra trùng địa chỉ
        if (homestayRepository.existsByStreetAndWardAndDistrict(
                request.getStreet(), request.getWard(), request.getDistrict())) {
            throw new DuplicateResourceException("Địa chỉ homestay đã tồn tại.");
        }

        // Lấy user hiện tại
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

        // Tạo homestay
        Homestay homestay = Homestay.builder()
                .name(request.getName())
                .street(request.getStreet())
                .ward(request.getWard())
                .district(request.getDistrict())
                .description(request.getDescription())
                .surfRating(0.0)
                .approveStatus("PENDING")
                .approvedBy(request.getApprovedBy())
                .contactInfo(request.getContactInfo())
                .host(user) // Gán host
                .build();

        homestayRepository.save(homestay);
        return mapToResponse(homestay);
    }

    @Override
    public HomestayResponse getHomestayById(Long id) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homestay not found"));
        return mapToResponse(homestay);
    }

    @Override
    public List<HomestayResponse> getAllHomestays() {
        List<Homestay> homestays = homestayRepository.findByApproveStatus("ACCEPTED");
        return homestays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<HomestayResponse> getHomestaysByHostId(Long hostId) {
        List<Homestay> homestays = homestayRepository.findByHost_Id(hostId);
        return homestays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HomestayResponse updateHomestay(Long id, HomestayRequest request) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homestay not found"));

        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Chỉ cho host sửa nếu là chủ homestay
        checkOwnership(homestay);

        homestay.setName(request.getName());
        homestay.setStreet(request.getStreet());
        homestay.setWard(request.getWard());
        homestay.setDistrict(request.getDistrict());
        homestay.setDescription(request.getDescription());
        homestay.setApprovedBy(request.getApprovedBy());
        homestay.setContactInfo(request.getContactInfo());

        homestayRepository.save(homestay);
        return mapToResponse(homestay);
    }

    @Override
    public void deleteHomestay(Long id) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homestay not found"));

        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        checkOwnership(homestay);

        List<Room> rooms = roomRepository.findByHomestay_HomestayId(homestay.getHomestayId());
        for (Room room : rooms) {
            roomService.deleteRoom(room.getRoomId());
        }

        List<HomestayImage> images = homestayImageRepository.findByHomestay_HomestayId(id);
        if (!images.isEmpty()) {
            homestayImageRepository.deleteAll(images);
        }


        homestayRepository.delete(homestay);
    }

    @Override
    public HomestayResponse pendingHomestay(Long id) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homestay not found"));
        homestay.setApproveStatus("ACCEPTED");
        homestayRepository.save(homestay);
        return mapToResponse(homestay);
    }

    @Override
    public List<HomestayResponse> getHomestayByHost(Long id) {
        List<Homestay> homestays = homestayRepository.findByHost_IdAndApproveStatusNot(id,"PENDING");
        return homestays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HomestayResponse> getMyHomestays() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new ForbiddenException("Bạn chưa đăng nhập hoặc token không hợp lệ");
        }

        List<Homestay> homestays = homestayRepository.findByHost_IdAndApproveStatus(userDetails.getId(),"ACCEPTED");
        return homestays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HomestayResponse> getMyPendingHomestays() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new ForbiddenException("Bạn chưa đăng nhập hoặc token không hợp lệ");
        }

        List<Homestay> homestays = homestayRepository.findByHost_IdAndApproveStatus(userDetails.getId(),"PENDING");
        return homestays.stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(HomestayResponse::getId).reversed())
                .collect(Collectors.toList());
    }

    public List<HomestayResponse> getMyRejectedHomestays() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new ForbiddenException("Bạn chưa đăng nhập hoặc token không hợp lệ");
        }

        List<Homestay> homestays = homestayRepository.findByHost_IdAndApproveStatus(userDetails.getId(),"REJECTED");
        return homestays.stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(HomestayResponse::getId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<HomestayResponse> getAllPendingHomestays() {
        List<Homestay> homestays = homestayRepository.findByApproveStatus("PENDING");
        return homestays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<HomestayResponse> getAllByDistrict(String district,String status) {
        List<Homestay> homestays = homestayRepository.findByDistrictAndApproveStatus(district,"ACCEPTED");
        return homestays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private HomestayResponse mapToResponse(Homestay homestay) {
        return HomestayResponse.builder()
                .id(homestay.getHomestayId())
                .name(homestay.getName())
                .street(homestay.getStreet())
                .ward(homestay.getWard())
                .district(homestay.getDistrict())
                .description(homestay.getDescription())
                .surfRating(homestay.getSurfRating())
                .approveStatus(homestay.getApproveStatus())
                .approvedBy(homestay.getApprovedBy())
                .contactInfo(homestay.getContactInfo())
                .createdAt(homestay.getCreatedAt())
                .build();
    }

    public Homestay findEntityById(Long id) {
        return homestayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));
    }

    @Override
    public HomestayResponse rejectHomestay(Long id) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homestay not found"));
        homestay.setApproveStatus("REJECTED");
        homestayRepository.save(homestay);
        return mapToResponse(homestay);
    }

    private void checkOwnership(Homestay homestay) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (homestay.getHost() != null && !homestay.getHost().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền thực hiện hành động này>");
        }
    }

    public List<HomestayResponse> searchHomestays(
            String roomType, Double priceFrom, Double priceTo,
            String features, LocalDate checkInDate, LocalDate checkOutDate,
            Double surfRating, String location) {

//        List<String> featureList = parseFeatures(features);

        List<Homestay> homestays = homestayRepository.searchHomestays(
                roomType, priceFrom, priceTo, features, checkInDate, checkOutDate, surfRating, location);

        List<HomestayResponse> results = new ArrayList<>();
        for (Homestay h : homestays) {
            HomestayResponse dto = HomestayResponse.builder()
                    .id(h.getHomestayId())
                    .name(h.getName())
                    .street(h.getStreet())
                    .ward(h.getWard())
                    .district(h.getDistrict())
                    .surfRating(h.getSurfRating())
                    .contactInfo(h.getContactInfo())
                    .approveStatus(h.getApproveStatus())
                    .build();
            results.add(dto);
        }
        return results;
    }

    private List<String> parseFeatures(String features) {
        if (features == null || features.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(features.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}