package com.hotel.service;

import com.hotel.dto.BookingRequest;
import com.hotel.dto.BookingView;
import com.hotel.exception.ApiException;
import com.hotel.model.Booking;
import com.hotel.model.BookingStatus;
import com.hotel.model.Hotel;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final HotelRepository hotels;
    private final BookingRepository bookings;

    public BookingService(HotelRepository hotels, BookingRepository bookings) {
        this.hotels = hotels;
        this.bookings = bookings;
    }

    @Transactional
    public BookingView create(BookingRequest r) {
        StayRules.validateDates(r.checkIn(), r.checkOut(), LocalDate.now());

        // Row lock on the hotel: two people booking the last room are processed one after the other.
        Hotel hotel = hotels.findByIdForUpdate(r.hotelId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hotel not found."));

        List<Booking> overlapping = bookings.findByHotel_IdAndStatusAndCheckInBeforeAndCheckOutAfter(
                hotel.getId(), BookingStatus.CONFIRMED, r.checkOut(), r.checkIn());
        if (StayRules.availableRooms(hotel.getTotalRooms(), overlapping, r.checkIn(), r.checkOut()) < 1) {
            throw new ApiException(HttpStatus.CONFLICT, "Sorry, no rooms are available for the selected dates.");
        }

        Booking b = new Booking();
        b.setReference("BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        b.setHotel(hotel);
        b.setGuestName(r.guestName().trim());
        b.setEmail(r.email().trim().toLowerCase());
        b.setCheckIn(r.checkIn());
        b.setCheckOut(r.checkOut());
        // Total is always computed here - the client never sends a price.
        b.setTotalAmount(StayRules.total(hotel.getPricePerNight(), StayRules.nights(r.checkIn(), r.checkOut())));
        b.setStatus(BookingStatus.CONFIRMED);
        b.setCreatedAt(Instant.now());
        return toView(bookings.save(b));
    }

    @Transactional(readOnly = true)
    public BookingView find(String reference, String email) {
        return toView(findOrThrow(reference, email));
    }

    @Transactional
    public BookingView cancel(String reference, String email) {
        Booking b = findOrThrow(reference, email);
        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "This booking is already cancelled.");
        }
        if (!b.getCheckIn().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.CONFLICT, "Bookings cannot be cancelled once the stay has started.");
        }
        // Availability only counts CONFIRMED bookings, so this releases the room.
        b.setStatus(BookingStatus.CANCELLED);
        return toView(b);
    }

    private Booking findOrThrow(String reference, String email) {
        return bookings.findByReferenceAndEmailIgnoreCase(reference.trim().toUpperCase(), email.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "No booking found for that reference and email."));
    }

    private BookingView toView(Booking b) {
        return new BookingView(b.getReference(), b.getHotel().getId(), b.getHotel().getName(), b.getGuestName(),
                b.getEmail(), b.getCheckIn(), b.getCheckOut(), StayRules.nights(b.getCheckIn(), b.getCheckOut()),
                b.getTotalAmount(), b.getStatus());
    }
}
