package com.homestay.homestayweb.utils;

import com.homestay.homestayweb.entity.Room;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingUtil {
    public static long calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (checkOut.isBefore(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        long days = ChronoUnit.DAYS.between(checkIn, checkOut);

        if (days == 0) {
            days = 1;
        }

        return (long) (days * room.getPrice());
    }
}

