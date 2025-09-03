package com.homestay.homestayweb.repository;

import com.homestay.homestayweb.entity.Homestay;
import com.homestay.homestayweb.entity.HomestayImage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomestayImageRepository extends JpaRepository<HomestayImage, Long> {
    HomestayImage findByHomestayAndIsPrimaryTrue(Homestay homestay);
    List<HomestayImage> findByHomestay_HomestayId(Long homestayId);
}