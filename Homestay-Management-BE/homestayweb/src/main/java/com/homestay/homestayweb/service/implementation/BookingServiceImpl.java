package com.homestay.homestayweb.service.implementation;

import com.homestay.homestayweb.dto.request.BookingRequest;
import com.homestay.homestayweb.dto.response.BookingResponse;
import com.homestay.homestayweb.entity.Booking;
import com.homestay.homestayweb.entity.Homestay;
import com.homestay.homestayweb.entity.Room;
import com.homestay.homestayweb.entity.User;
import com.homestay.homestayweb.exception.ForbiddenException;
import com.homestay.homestayweb.exception.ResourceNotFoundException;
import com.homestay.homestayweb.repository.BookingRepository;
import com.homestay.homestayweb.repository.RoomRepository;
import com.homestay.homestayweb.repository.UserRepository;
import com.homestay.homestayweb.security.UserDetailsImpl;
import com.homestay.homestayweb.service.BookingService;
import com.homestay.homestayweb.utils.BookingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return mapToResponse(booking);
    }

    @Override
    public BookingResponse createBooking(BookingRequest request, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .totalPrice(BookingUtil.calculatePrice(room, request.getCheckInDate(), request.getCheckOutDate()))
                .bookingStatus("PENDING")
                .createdAt(LocalDate.now())
                .build();

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    @Override
    public BookingResponse pendingBooking(Long id) {
        Booking book = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (isBookingOverlapping(id)) {
            throw new IllegalStateException("Phòng đã được đặt trong khoảng thời gian này.");
        }


        book.setBookingStatus("ACCEPTED");
        bookingRepository.save(book);
        return mapToResponse(book);
    }

    @Override
    public BookingResponse rejectBooking(Long id) {
        Booking book = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        book.setBookingStatus("REJECTED");
        bookingRepository.save(book);
        return mapToResponse(book);
    }

    @Override
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingResponse> getAcceptedBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_IdAndBookingStatus(userId,"ACCEPTED").stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(BookingResponse::getBookingId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getRejectedBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_IdAndBookingStatus(userId,"REJECTED").stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(BookingResponse::getBookingId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getPendingBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_IdAndBookingStatus(userId,"PENDING").stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(BookingResponse::getBookingId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoom_RoomId(roomId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsForHost(Long hostId) {
        return bookingRepository.findPendingByHostId(hostId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void checkOwnership(Booking booking) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (booking.getRoom().getHomestay().getHost() != null && !booking.getRoom().getHomestay().getHost().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền thực hiện hành động này>");
        }
    }

    @Override
    public List<BookingResponse> filterBookingsForHost(Long bookingId, LocalDate checkInDate, LocalDate checkOutDate, Long roomId, LocalDate createdAt, String userEmail, String homestayName) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new ForbiddenException("Bạn chưa đăng nhập hoặc token không hợp lệ");
        }

        List<Booking> responses = bookingRepository.searchBooking(userDetails.getId(), bookingId, checkInDate, checkOutDate, roomId, createdAt, userEmail, homestayName);

        List<BookingResponse> results = new ArrayList<>();
        for (Booking b : responses) {
        BookingResponse dto = BookingResponse.builder()
                .bookingId(b.getBookingId())
                .checkInDate(b.getCheckInDate())
                .checkOutDate(b.getCheckOutDate())
                .totalPrice(b.getTotalPrice())
                .userId(b.getUser().getId())
                .roomId(b.getRoom().getRoomId())
                .createdAt(b.getCreatedAt())
                .userEmail(b.getUser().getEmail())
                .homestayId(b.getRoom().getHomestay().getHomestayId())
                .homestayName(b.getRoom().getHomestay().getName()).build();
        results.add(dto);
        }

        return results;
    }

    @Override
    public boolean isBookingOverlapping(Long bookingId) {
        // Lấy thông tin booking ban đầu
        Booking currentBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        Long roomId = currentBooking.getRoom().getRoomId();
        LocalDate checkIn = currentBooking.getCheckInDate();
        LocalDate checkOut = currentBooking.getCheckOutDate();

        // Lấy tất cả booking khác của cùng phòng
        List<Booking> overlappingBookings = bookingRepository.findByRoom_RoomIdAndBookingIdNotAndBookingStatus(roomId, bookingId, "ACCEPTED");

        for (Booking other : overlappingBookings) {
            LocalDate otherCheckIn = other.getCheckInDate();
            LocalDate otherCheckOut = other.getCheckOutDate();

            // Kiểm tra nếu khoảng thời gian giao nhau
            if (!(checkOut.isBefore(otherCheckIn) || checkIn.isAfter(otherCheckOut))) {
                return true; // Có trùng lịch
            }
        }

        return false; // Không trùng lịch
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalPrice(booking.getTotalPrice())
                .userId(booking.getUser().getId())
                .userEmail(booking.getUser().getEmail())
                .roomId(booking.getRoom().getRoomId())
                .homestayId(booking.getRoom().getHomestay().getHomestayId())
                .homestayName(booking.getRoom().getHomestay().getName())
                .createdAt(booking.getCreatedAt())
                .build();
    }

}
