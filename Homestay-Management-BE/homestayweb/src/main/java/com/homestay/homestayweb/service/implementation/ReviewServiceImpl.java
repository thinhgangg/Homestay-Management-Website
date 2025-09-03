package com.homestay.homestayweb.service.implementation;

import com.homestay.homestayweb.dto.request.ReviewRequest;
import com.homestay.homestayweb.entity.Homestay;
import com.homestay.homestayweb.entity.Review;
import com.homestay.homestayweb.entity.User;
import com.homestay.homestayweb.repository.HomestayRepository;
import com.homestay.homestayweb.repository.ReviewRepository;
import com.homestay.homestayweb.repository.UserRepository;
import com.homestay.homestayweb.security.UserDetailsImpl;
import com.homestay.homestayweb.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Override
    public Review createReview(ReviewRequest reviewRequest) {
        // Lấy username hiện tại từ Spring Security
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails)
                ? ((UserDetailsImpl) principal).getEmail()
                : principal.toString();

        // Tìm User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm Homestay
        Homestay homestay = homestayRepository.findById(reviewRequest.getHomestayId())
                .orElseThrow(() -> new RuntimeException("Homestay not found"));

        // Tạo và lưu review
        Review review = new Review();
        review.setUser(user);
        review.setHomestay(homestay);
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Tính trung bình rating mới và cập nhật Homestay
        Double averageRating = reviewRepository.findAverageRatingByHomestayId(homestay.getHomestayId());
        homestay.setSurfRating(averageRating);
        homestayRepository.save(homestay);

        return savedReview;
    }

    @Override
    public Double getAverageRating(Long homestayId) {
        return reviewRepository.findAverageRatingByHomestayId(homestayId);
    }

    @Override
    public List<Review> getReviewsByHomestay(Long homestayId) {
        return  reviewRepository.findByHomestayHomestayId(homestayId);
    }
}
