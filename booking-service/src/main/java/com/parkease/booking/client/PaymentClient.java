package com.parkease.booking.client;

import com.parkease.booking.dto.external.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/payments")
    void initiatePayment(@RequestBody PaymentRequest request);
}
