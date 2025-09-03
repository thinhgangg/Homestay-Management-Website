package com.homestay.homestayweb.service;

import com.homestay.homestayweb.dto.request.ReviewRequest;
import com.homestay.homestayweb.entity.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(ReviewRequest reviewRequest);
    List<Review> getReviewsByHomestay(Long homestayId);
    Double getAverageRating(Long homestayId);
}