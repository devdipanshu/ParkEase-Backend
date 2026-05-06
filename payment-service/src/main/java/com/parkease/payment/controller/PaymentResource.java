package com.parkease.payment.controller;

import com.parkease.payment.dto.PaymentRequest;
import com.parkease.payment.dto.PaymentResponse;
import com.parkease.payment.dto.RefundRequest;
import com.parkease.payment.entity.Payment;
import com.parkease.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentResource {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentResponse.from(paymentService.processPayment(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.getPaymentById(id)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.getPaymentByBookingId(bookingId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id,
                                                          @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.refundPayment(id, request)));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {
        byte[] pdf = paymentService.generateReceipt(id);
        Payment payment = paymentService.getPaymentById(id);
        String filename = "receipt_" + payment.getTransactionId() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/revenue/lot/{lotId}")
    public ResponseEntity<Double> getRevenueByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(paymentService.getTotalRevenueByLot(lotId));
    }

    @GetMapping("/revenue")
    public ResponseEntity<Double> getRevenueBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(paymentService.getRevenueBetween(start, end));
    }
}
