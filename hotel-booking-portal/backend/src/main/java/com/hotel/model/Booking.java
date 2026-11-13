package com.hotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter @Setter
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reference;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
    private String guestName;
    private String email;
    private LocalDate checkIn;
    private LocalDate checkOut;        // exclusive: guest leaves this morning
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private Instant createdAt;
}
