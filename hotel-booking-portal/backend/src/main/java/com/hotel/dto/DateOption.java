package com.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Surprise feature: an alternative stay (same length) that still has a room. */
public record DateOption(LocalDate checkIn, LocalDate checkOut, BigDecimal totalAmount) {}
