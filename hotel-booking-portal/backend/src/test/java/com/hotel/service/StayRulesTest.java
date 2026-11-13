package com.hotel.service;

import static org.junit.jupiter.api.Assertions.*;

import com.hotel.exception.ApiException;
import com.hotel.model.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StayRulesTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 10);

    private static Booking stay(String in, String out) {
        Booking b = new Booking();
        b.setCheckIn(LocalDate.parse(in));
        b.setCheckOut(LocalDate.parse(out));
        return b;
    }

    @Test
    void totalIsNightsTimesPrice() {
        long nights = StayRules.nights(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 4));
        assertEquals(3, nights);
        assertEquals(0, new BigDecimal("19500.00").compareTo(StayRules.total(new BigDecimal("6500.00"), nights)));
    }

    @Test
    void rejectsCheckInInThePast() {
        assertThrows(ApiException.class,
                () -> StayRules.validateDates(TODAY.minusDays(1), TODAY.plusDays(2), TODAY));
    }

    @Test
    void rejectsCheckOutNotAfterCheckIn() {
        assertThrows(ApiException.class, () -> StayRules.validateDates(TODAY, TODAY, TODAY));
        assertThrows(ApiException.class, () -> StayRules.validateDates(TODAY.plusDays(3), TODAY.plusDays(1), TODAY));
    }

    @Test
    void acceptsCheckInToday() {
        assertDoesNotThrow(() -> StayRules.validateDates(TODAY, TODAY.plusDays(1), TODAY));
    }

    @Test
    void backToBackStaysDoNotOverlap() {
        List<Booking> existing = List.of(stay("2026-03-01", "2026-03-05"));
        assertEquals(1, StayRules.availableRooms(1, existing, LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 7)));
    }

    @Test
    void fullHotelHasNoAvailability() {
        List<Booking> existing = List.of(stay("2026-03-01", "2026-03-05"), stay("2026-03-02", "2026-03-04"));
        assertEquals(0, StayRules.availableRooms(2, existing, LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 4)));
    }

    @Test
    void availabilityUsesBusiestNightNotSumOfBookings() {
        // Two bookings that never share a night: only one room is ever used at a time.
        List<Booking> existing = List.of(stay("2026-03-01", "2026-03-03"), stay("2026-03-03", "2026-03-05"));
        assertEquals(1, StayRules.peakOccupancy(existing, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5)));
        assertEquals(1, StayRules.availableRooms(2, existing, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5)));
    }

    @Test
    void partialOverlapReducesAvailability() {
        List<Booking> existing = List.of(stay("2026-03-04", "2026-03-10"));
        assertEquals(0, StayRules.availableRooms(1, existing, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5)));
    }
}
