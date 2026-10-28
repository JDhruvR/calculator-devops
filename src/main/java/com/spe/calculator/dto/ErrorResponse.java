package com.spe.calculator.dto;

/**
 * Standard JSON error response returned on HTTP 400.
 * Example: {"status":400,"error":"Bad Request","message":"Division by zero"}
 */
public record ErrorResponse(
        int status,
        String error,
        String message
) {}

