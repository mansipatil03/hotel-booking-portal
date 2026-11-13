package com.hotel.exception;

import com.hotel.dto.ApiError;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Every failure leaves the API in the same JSON shape (ApiError). */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApi(ApiException e) {
        return build(e.getStatus(), e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, String.join(". ", details) + ".", details);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> handleMissing(MissingServletRequestParameterException e) {
        return build(HttpStatus.BAD_REQUEST, "Missing required value: " + e.getParameterName(), List.of());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> handleBadInput(Exception e) {
        return build(HttpStatus.BAD_REQUEST, "Invalid input. Dates must look like YYYY-MM-DD.", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleOther(Exception e) {
        log.error("Unexpected error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.", List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus s, String msg, List<String> details) {
        return ResponseEntity.status(s).body(new ApiError(s.value(), s.getReasonPhrase(), msg, details));
    }
}
