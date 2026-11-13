package com.hotel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "Hotel is required") Long hotelId,
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name is too long") String guestName,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") @Size(max = 150, message = "Email is too long") String email,
        @NotNull(message = "Check-in date is required") LocalDate checkIn,
        @NotNull(message = "Check-out date is required") LocalDate checkOut) {}
