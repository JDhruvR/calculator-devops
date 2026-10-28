package com.spe.calculator.controller;

import com.spe.calculator.dto.CalculatorResponse;
import com.spe.calculator.dto.ErrorResponse;
import com.spe.calculator.service.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the four arithmetic endpoints under /api/v1.
 *
 * All operations accept query parameters a and b (both required doubles).
 * Division by zero returns HTTP 400 with a structured error body.
 */
@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculatorService;

    @Autowired
    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    // GET /api/v1/add?a=<num>&b=<num>
    @GetMapping("/add")
    public ResponseEntity<CalculatorResponse> add(
            @RequestParam double a,
            @RequestParam double b) {
        return ResponseEntity.ok(
                new CalculatorResponse("add", a, b, calculatorService.add(a, b))
        );
    }

    // GET /api/v1/subtract?a=<num>&b=<num>
    @GetMapping("/subtract")
    public ResponseEntity<CalculatorResponse> subtract(
            @RequestParam double a,
            @RequestParam double b) {
        return ResponseEntity.ok(
                new CalculatorResponse("subtract", a, b, calculatorService.subtract(a, b))
        );
    }

    // GET /api/v1/multiply?a=<num>&b=<num>
    @GetMapping("/multiply")
    public ResponseEntity<CalculatorResponse> multiply(
            @RequestParam double a,
            @RequestParam double b) {
        return ResponseEntity.ok(
                new CalculatorResponse("multiply", a, b, calculatorService.multiply(a, b))
        );
    }

    // GET /api/v1/divide?a=<num>&b=<num>  — b=0 returns HTTP 400
    @GetMapping("/divide")
    public ResponseEntity<?> divide(
            @RequestParam double a,
            @RequestParam double b) {
        try {
            return ResponseEntity.ok(
                    new CalculatorResponse("divide", a, b, calculatorService.divide(a, b))
            );
        } catch (ArithmeticException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }
}
