package com.hotel.service;

import com.hotel.exception.ApiException;
import com.hotel.model.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;

/** Pure booking rules (no database) so they are easy to unit test. */
public final class StayRules {
    private StayRules() {}

    public static long nights(LocalDate checkIn, LocalDate checkOut) {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public static BigDecimal total(BigDecimal pricePerNight, long nights) {
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    public static void validateDates(LocalDate checkIn, LocalDate checkOut, LocalDate today) {
        if (checkIn == null || checkOut == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(today)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in.");
        }
    }

    /**
     * Highest number of rooms in use on any single night of [checkIn, checkOut).
     * A booking occupies the nights checkIn .. checkOut-1, so back-to-back stays
     * (one checks out the day another checks in) do not clash.
     */
    public static int peakOccupancy(List<Booking> confirmed, LocalDate checkIn, LocalDate checkOut) {
        int peak = 0;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            int used = 0;
            for (Booking b : confirmed) {
                if (!night.isBefore(b.getCheckIn()) && night.isBefore(b.getCheckOut())) used++;
            }
            peak = Math.max(peak, used);
        }
        return peak;
    }

    public static int availableRooms(int totalRooms, List<Booking> confirmed, LocalDate checkIn, LocalDate checkOut) {
        return Math.max(0, totalRooms - peakOccupancy(confirmed, checkIn, checkOut));
    }
}
