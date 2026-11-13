package com.hotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hotels")
@Getter @Setter
public class Hotel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private String description;
    private String amenities;          // comma separated
    private BigDecimal pricePerNight;
    private int totalRooms;
}
