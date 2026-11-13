package com.hotel.controller;

import com.hotel.dto.BookingRequest;
import com.hotel.dto.BookingView;
import com.hotel.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingView create(@Valid @RequestBody BookingRequest request) {
        return service.create(request);
    }

    @GetMapping("/{reference}")
    public BookingView get(@PathVariable String reference, @RequestParam String email) {
        return service.find(reference, email);
    }

    @PostMapping("/{reference}/cancel")
    public BookingView cancel(@PathVariable String reference, @RequestParam String email) {
        return service.cancel(reference, email);
    }
}
