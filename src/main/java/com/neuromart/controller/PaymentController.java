package com.neuromart.controller;

import com.neuromart.dto.PaymentProcessRequest;
import com.neuromart.dto.PaymentVerificationRequest;
import com.neuromart.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment/process")
    public ResponseEntity<Map<String, Object>> processPayment(@Valid @RequestBody PaymentProcessRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @GetMapping("/getKey")
    public ResponseEntity<Map<String, String>> sendAPIKey() {
        return ResponseEntity.ok(paymentService.getApiKey());
    }

    @PostMapping("/paymentVerification")
    public ResponseEntity<Map<String, Object>> paymentVerification(@RequestBody PaymentVerificationRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }
}
