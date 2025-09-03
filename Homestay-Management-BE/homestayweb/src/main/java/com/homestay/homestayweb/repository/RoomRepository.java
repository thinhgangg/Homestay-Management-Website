package com.homestay.homestayweb.repository;

import com.homestay.homestayweb.dto.response.RoomResponse;
import com.homestay.homestayweb.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHomestay_HomestayId(Long homestayId);

    List<Room> findByAvailability(Boolean availability);
    List<Room> findByRoomStatus(String status);

    @Query(value = """
    SELECT * FROM room r
    WHERE r.homestay_id = :homestayId
      AND r.availability = '1'
      AND r.room_id NOT IN (
      SELECT b.room_id FROM booking b
        WHERE b.check_in_date < :checkOutDate
        AND b.check_out_date > :checkInDate
        AND b.booking_status = 'ACCEPTED'
      )
    """, nativeQuery = true)
    List<Room> findAvailableRooms(
            @Param("homestayId") Long homestayId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

}
