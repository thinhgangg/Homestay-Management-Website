package com.homestay.homestayweb.repository;

import com.homestay.homestayweb.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByBooking_BookingId(Long bookingId);
    void deleteByBooking_BookingId(Long id);

    // Thống kê doanh thu theo ngày cho host hiện tại
    @Query(value = "SELECT DATE(p.created_at) as day, SUM(p.amount) as totalRevenue " +
            "FROM payment p " +
            "JOIN booking b ON p.booking_id = b.booking_id " +
            "JOIN room r ON b.room_id = r.room_id " +
            "JOIN homestay h ON r.homestay_id = h.homestay_id " +
            "WHERE h.host_id = :hostId AND p.payment_status = 'Completed' " +
            "GROUP BY day " +
            "ORDER BY day ASC", nativeQuery = true)
    List<Object[]> getDailyRevenueByHost(@Param("hostId") Long hostId);

    // Thống kê doanh thu theo homestay cho host hiện tại
    @Query(value = """
    SELECT h.name AS homestayName, 
           COALESCE(SUM(p.amount), 0) AS totalRevenue
    FROM homestay h
    LEFT JOIN room r ON h.homestay_id = r.homestay_id
    LEFT JOIN booking b ON r.room_id = b.room_id
    LEFT JOIN payment p ON b.booking_id = p.booking_id AND p.payment_status = 'Completed'
    WHERE h.host_id = :hostId
    AND h.approve_status = 'ACCEPTED'
    GROUP BY h.name
    ORDER BY totalRevenue DESC
    """, nativeQuery = true)
    List<Object[]> getRevenueByHomestayByHost(@Param("hostId") Long hostId);
}
