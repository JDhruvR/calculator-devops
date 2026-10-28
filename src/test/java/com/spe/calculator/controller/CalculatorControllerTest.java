package com.spe.calculator.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CalculatorController Integration Tests")
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Add ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/add?a=2&b=3 → 200 with result 5.0")
    void testAdd() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.a").value(2.0))
                .andExpect(jsonPath("$.b").value(3.0))
                .andExpect(jsonPath("$.result").value(5.0));
    }

    // ── Subtract ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/subtract?a=10&b=3 → 200 with result 7.0")
    void testSubtract() throws Exception {
        mockMvc.perform(get("/api/v1/subtract").param("a", "10").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("subtract"))
                .andExpect(jsonPath("$.result").value(7.0));
    }

    // ── Multiply ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/multiply?a=4&b=5 → 200 with result 20.0")
    void testMultiply() throws Exception {
        mockMvc.perform(get("/api/v1/multiply").param("a", "4").param("b", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("multiply"))
                .andExpect(jsonPath("$.result").value(20.0));
    }

    // ── Divide ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/divide?a=10&b=4 → 200 with result 2.5")
    void testDivide() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "10").param("b", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("divide"))
                .andExpect(jsonPath("$.result").value(2.5));
    }

    @Test
    @DisplayName("GET /api/v1/divide?a=5&b=0 → 400 Bad Request")
    void testDivideByZero() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "5").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/add?a=5 (missing b) → 400 Bad Request")
    void testMissingParameter() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/add?a=foo&b=3 (invalid type) → 400 Bad Request")
    void testInvalidParameterType() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "foo").param("b", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── Actuator ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /actuator/health → 200 with status UP")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}

