package com.spe.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CalculatorService Unit Tests")
class CalculatorServiceTest {

    private CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new CalculatorService();
    }

    @Test
    @DisplayName("Add: 2 + 3 = 5")
    void testAdd() {
        assertEquals(5.0, service.add(2, 3));
    }

    @Test
    @DisplayName("Add: negative numbers")
    void testAddNegative() {
        assertEquals(-1.0, service.add(-4, 3));
    }

    @Test
    @DisplayName("Subtract: 10 - 3 = 7")
    void testSubtract() {
        assertEquals(7.0, service.subtract(10, 3));
    }

    @Test
    @DisplayName("Subtract: result is negative")
    void testSubtractNegativeResult() {
        assertEquals(-1.0, service.subtract(2, 3));
    }

    @Test
    @DisplayName("Multiply: 4 * 5 = 20")
    void testMultiply() {
        assertEquals(20.0, service.multiply(4, 5));
    }

    @Test
    @DisplayName("Multiply: by zero")
    void testMultiplyByZero() {
        assertEquals(0.0, service.multiply(100, 0));
    }

    @Test
    @DisplayName("Divide: 10 / 4 = 2.5")
    void testDivide() {
        assertEquals(2.5, service.divide(10, 4));
    }

    @Test
    @DisplayName("Divide: by zero throws ArithmeticException")
    void testDivideByZero() {
        ArithmeticException ex = assertThrows(
                ArithmeticException.class,
                () -> service.divide(5, 0)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("zero"));
    }
}

