package com.homestay.homestayweb.repository;

import com.homestay.homestayweb.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByHomestayHomestayId(Long homestayId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.homestay.homestayId = :homestayId")
    Double findAverageRatingByHomestayId(@Param("homestayId") Long homestayId);
}
