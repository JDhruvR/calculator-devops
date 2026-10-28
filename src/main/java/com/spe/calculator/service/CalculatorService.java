package com.spe.calculator.service;

import org.springframework.stereotype.Service;

/**
 * Core calculator service containing all arithmetic logic.
 * Throws ArithmeticException for division by zero — the controller
 * maps this to an HTTP 400 response.
 */
@Service
public class CalculatorService {

    public double add(double a, double b) {
        return a + b;
    }

    public double subtract(double a, double b) {
        return a - b;
    }

    public double multiply(double a, double b) {
        return a * b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        return a / b;
    }
}

