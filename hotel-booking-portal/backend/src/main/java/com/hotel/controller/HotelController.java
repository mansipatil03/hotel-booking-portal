package com.hotel.controller;

import com.hotel.dto.HotelView;
import com.hotel.service.HotelService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final HotelService service;

    public HotelController(HotelService service) { this.service = service; }

    @GetMapping
    public List<HotelView> search(@RequestParam(required = false) String location,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return service.search(location, checkIn, checkOut);
    }

    @GetMapping("/{id}")
    public HotelView get(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return service.get(id, checkIn, checkOut);
    }
}
