package com.hotel.repository;

import com.hotel.model.Booking;
import com.hotel.model.BookingStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    /** Stays overlapping [in, out): existing.checkIn < out AND existing.checkOut > in. */
    List<Booking> findByHotel_IdAndStatusAndCheckInBeforeAndCheckOutAfter(
            Long hotelId, BookingStatus status, LocalDate out, LocalDate in);

    Optional<Booking> findByReferenceAndEmailIgnoreCase(String reference, String email);
}
