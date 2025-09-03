package com.homestay.homestayweb.repository;

import com.homestay.homestayweb.dto.response.BookingResponse;
import com.homestay.homestayweb.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser_IdAndBookingStatus(Long userId, String status);
    List<Booking> findByRoom_RoomId(Long roomId);
    @Query("SELECT b FROM Booking b WHERE b.room.homestay.host.id = :hostId AND b.bookingStatus = 'PENDING'")
    List<Booking> findPendingByHostId(@Param("hostId") Long hostId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.bookingStatus = 'ACCEPTED'
        AND b.room.homestay.host.id = :hostId
        AND (:bookingId IS NULL OR b.bookingId = :bookingId)
        AND (:checkInDate IS NULL OR b.checkInDate = :checkInDate)
        AND (:checkOutDate IS NULL OR b.checkOutDate = :checkOutDate)
        AND (:roomId IS NULL OR b.room.roomId = :roomId)
        AND (:createdAt IS NULL OR b.createdAt = :createdAt)
        AND (:userEmail IS NULL OR LOWER(b.user.email) LIKE LOWER(CONCAT('%', :userEmail, '%')))
        AND (:homestayName IS NULL OR LOWER(b.room.homestay.name) LIKE LOWER(CONCAT('%', :homestayName, '%')))
    """)
     List<Booking> searchBooking(
            @Param("hostId") Long hostId,
            @Param("bookingId") Long bookingId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomId") Long roomId,
            @Param("createdAt") LocalDate createdAt,
            @Param("userEmail") String userEmail,
            @Param("homestayName") String homestayName
    );

    List<Booking> findByRoom_RoomIdAndBookingIdNotAndBookingStatus(Long roomId, Long bookingId, String status);
}