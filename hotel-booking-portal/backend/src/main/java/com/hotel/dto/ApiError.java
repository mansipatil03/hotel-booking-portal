package com.hotel.dto;

import java.time.Instant;
import java.util.List;

public record ApiError(int status, String error, String message, List<String> details, Instant timestamp) {
    public ApiError(int status, String error, String message, List<String> details) {
        this(status, error, message, details, Instant.now());
    }
}
