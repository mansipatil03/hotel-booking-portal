package com.hotel.dto;

import java.math.BigDecimal;
import java.util.List;

/** availableRooms / nights / totalAmount are null until dates are supplied. */
public record HotelView(Long id, String name, String location, String description, List<String> amenities,
                        BigDecimal pricePerNight, Integer availableRooms, Long nights, BigDecimal totalAmount,
                        List<DateOption> alternatives) {}
