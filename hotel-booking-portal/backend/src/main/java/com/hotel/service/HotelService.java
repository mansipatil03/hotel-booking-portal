package com.hotel.service;

import com.hotel.dto.DateOption;
import com.hotel.dto.HotelView;
import com.hotel.exception.ApiException;
import com.hotel.model.Booking;
import com.hotel.model.BookingStatus;
import com.hotel.model.Hotel;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HotelService {
    private static final int ALT_WINDOW_DAYS = 14;
    private static final int MAX_ALTERNATIVES = 3;

    private final HotelRepository hotels;
    private final BookingRepository bookings;

    public HotelService(HotelRepository hotels, BookingRepository bookings) {
        this.hotels = hotels;
        this.bookings = bookings;
    }

    public List<HotelView> search(String location, LocalDate checkIn, LocalDate checkOut) {
        List<Hotel> found = (location == null || location.isBlank())
                ? hotels.findAll() : hotels.findByLocationContainingIgnoreCase(location.trim());
        return found.stream().map(h -> toView(h, checkIn, checkOut, false)).toList();
    }

    public HotelView get(Long id, LocalDate checkIn, LocalDate checkOut) {
        Hotel h = hotels.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hotel not found."));
        return toView(h, checkIn, checkOut, true);
    }

    private HotelView toView(Hotel h, LocalDate in, LocalDate out, boolean withAlternatives) {
        Integer available = null;
        Long nights = null;
        BigDecimal total = null;
        List<DateOption> options = List.of();

        if (in != null || out != null) {
            StayRules.validateDates(in, out, LocalDate.now());
            nights = StayRules.nights(in, out);
            total = StayRules.total(h.getPricePerNight(), nights);
            List<Booking> overlapping = bookings
                    .findByHotel_IdAndStatusAndCheckInBeforeAndCheckOutAfter(h.getId(), BookingStatus.CONFIRMED, out, in);
            available = StayRules.availableRooms(h.getTotalRooms(), overlapping, in, out);
            if (withAlternatives && available == 0) options = alternatives(h, in, out, nights);
        }
        List<String> amenities = Arrays.stream(h.getAmenities().split(",")).map(String::trim).toList();
        return new HotelView(h.getId(), h.getName(), h.getLocation(), h.getDescription(), amenities,
                h.getPricePerNight(), available, nights, total, options);
    }

    /** Surprise feature: when sold out, suggest the closest same-length stays that have a room. */
    private List<DateOption> alternatives(Hotel h, LocalDate in, LocalDate out, long nights) {
        LocalDate today = LocalDate.now();
        List<Booking> nearby = bookings.findByHotel_IdAndStatusAndCheckInBeforeAndCheckOutAfter(
                h.getId(), BookingStatus.CONFIRMED, out.plusDays(ALT_WINDOW_DAYS), in.minusDays(ALT_WINDOW_DAYS));
        BigDecimal total = StayRules.total(h.getPricePerNight(), nights);
        List<DateOption> result = new ArrayList<>();
        for (int d = 1; d <= ALT_WINDOW_DAYS && result.size() < MAX_ALTERNATIVES; d++) {
            for (int sign : new int[]{1, -1}) {
                LocalDate start = in.plusDays((long) sign * d);
                LocalDate end = start.plusDays(nights);
                if (start.isBefore(today) || result.size() >= MAX_ALTERNATIVES) continue;
                if (StayRules.availableRooms(h.getTotalRooms(), nearby, start, end) > 0) {
                    result.add(new DateOption(start, end, total));
                }
            }
        }
        return result;
    }
}
