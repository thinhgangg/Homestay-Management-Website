package com.homestay.homestayweb.repository;

import com.homestay.homestayweb.entity.Room;
import com.homestay.homestayweb.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoom_RoomId(Long roomId);
}