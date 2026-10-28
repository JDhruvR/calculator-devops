package com.spe.calculator.dto;

/**
 * Standard JSON response for all calculator operations.
 * Example: {"operation":"add","a":2.0,"b":3.0,"result":5.0}
 */
public record CalculatorResponse(
        String operation,
        double a,
        double b,
        double result
) {}
