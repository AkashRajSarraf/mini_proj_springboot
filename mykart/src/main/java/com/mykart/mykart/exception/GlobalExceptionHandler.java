package com.mykart.mykart.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        // If a specific known exception is wrapped (e.g. by transaction proxies), unwrap and handle it
        Throwable cause = findCause(ex, InsufficientStockException.class);
        if (cause != null) {
            log.debug("Unwrapped InsufficientStockException from: {}", ex.getClass().getSimpleName());
            return handleInsufficientStock((InsufficientStockException) cause, request);
        }

        cause = findCause(ex, ResourceNotFoundException.class);
        if (cause != null) {
            log.debug("Unwrapped ResourceNotFoundException from: {}", ex.getClass().getSimpleName());
            return handleNotFound((ResourceNotFoundException) cause, request);
        }

        cause = findCause(ex, IllegalArgumentException.class);
        if (cause != null) {
            log.debug("Unwrapped IllegalArgumentException from: {}", ex.getClass().getSimpleName());
            return handleIllegalArgument((IllegalArgumentException) cause, request);
        }

        log.error("Unhandled exception caught: {}", ex.toString(), ex);
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Throwable findCause(Throwable t, Class<?> target) {
        Throwable current = t;
        while (current != null) {
            if (target.isInstance(current)) return current;
            current = current.getCause();
        }
        return null;
    }
}
