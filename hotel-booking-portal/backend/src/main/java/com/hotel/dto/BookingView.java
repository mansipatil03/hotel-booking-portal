package com.hotel.dto;

import com.hotel.model.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingView(String reference, Long hotelId, String hotelName, String guestName, String email,
                          LocalDate checkIn, LocalDate checkOut, long nights, BigDecimal totalAmount,
                          BookingStatus status) {}
