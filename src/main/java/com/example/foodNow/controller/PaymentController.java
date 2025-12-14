package com.example.foodNow.controller;

import com.example.foodNow.dto.PaymentRequest;
import com.example.foodNow.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Random;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/simulate")
    public ResponseEntity<PaymentResponse> simulatePayment(@RequestBody PaymentRequest request) {
        if (request.getAmount().doubleValue() <= 0) {
            return ResponseEntity.badRequest().body(new PaymentResponse("FAILED", "Invalid amount"));
        }

        if ("CASH_ON_DELIVERY".equals(request.getPaymentMethod())) {
            return ResponseEntity.ok(new PaymentResponse("SUCCESS", "Cash on delivery confirmed."));
        }

        if ("CARD_SIMULATION".equals(request.getPaymentMethod())) {
            // Simulate 90% success rate
            boolean isSuccess = new Random().nextInt(10) > 0;
            if (isSuccess) {
                return ResponseEntity.ok(new PaymentResponse("SUCCESS", "Payment processed successfully."));
            } else {
                return ResponseEntity.ok(new PaymentResponse("FAILED", "Card declined (Simulated)."));
            }
        }

        return ResponseEntity.badRequest().body(new PaymentResponse("FAILED", "Unknown payment method"));
    }
}
