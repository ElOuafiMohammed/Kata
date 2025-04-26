package com.example.kata.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ErrorResponse handleTimeSlotNotAvailable(TimeSlotNotAvailableException ex, WebRequest request) {
        logger.error("Time slot not available: {}", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Time Slot Not Available",
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(CompletionException.class)
    public ErrorResponse handleCompletionException(CompletionException ex, WebRequest request) {
        Throwable cause = ex.getCause();
        logger.error("Async operation failed: {}", cause != null ? cause.getMessage() : ex.getMessage());

        if (cause instanceof TimeSlotNotAvailableException) {
            return handleTimeSlotNotAvailable((TimeSlotNotAvailableException) cause, request);
        }

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Async Operation Failed",
                cause != null ? cause.getMessage() : ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred: {}", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}