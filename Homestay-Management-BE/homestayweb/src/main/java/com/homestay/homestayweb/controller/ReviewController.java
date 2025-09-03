package com.homestay.homestayweb.controller;

import com.homestay.homestayweb.dto.request.ReviewRequest;
import com.homestay.homestayweb.dto.response.ReviewResponse;
import com.homestay.homestayweb.entity.Review;
import com.homestay.homestayweb.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ReviewResponse createReview(@RequestBody ReviewRequest request) {
        Review review = reviewService.createReview(request);
        return toResponse(review);
    }

    @GetMapping("/homestay/{homestayId}")
    public List<ReviewResponse> getByHomestay(@PathVariable Long homestayId) {
        return reviewService.getReviewsByHomestay(homestayId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse dto = new ReviewResponse();
        dto.setReviewId(review.getReviewId());
        dto.setUserName(review.getUser().getUsername());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
