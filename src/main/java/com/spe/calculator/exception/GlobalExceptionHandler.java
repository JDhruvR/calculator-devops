package com.spe.calculator.exception;

import com.spe.calculator.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized exception handler that converts common Spring MVC exceptions
 * into structured HTTP 400 JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles missing query parameters, e.g. GET /add?a=5 (b is missing).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        400,
                        "Bad Request",
                        "Missing required parameter: " + ex.getParameterName()
                ));
    }

    /**
     * Handles non-numeric parameter values, e.g. GET /add?a=foo&b=3.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        400,
                        "Bad Request",
                        "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue()
                ));
    }
}

