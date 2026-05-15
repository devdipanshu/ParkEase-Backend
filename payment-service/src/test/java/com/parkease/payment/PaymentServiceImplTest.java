package com.parkease.payment;

import com.parkease.payment.dto.RefundRequest;
import com.parkease.payment.entity.Payment;
import com.parkease.payment.enums.PaymentMode;
import com.parkease.payment.enums.PaymentStatus;
import com.parkease.payment.exception.ResourceNotFoundException;
import com.parkease.payment.repository.PaymentRepository;
import com.parkease.payment.service.PaymentServiceImpl;
import com.parkease.payment.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceiptService receiptService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment paidPayment;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        paidPayment = Payment.builder()
                .paymentId(1L)
                .bookingId(10L)
                .userId(100L)
                .lotId(5L)
                .amount(150.0)
                .status(PaymentStatus.PAID)
                .mode(PaymentMode.CARD)
                .transactionId("TXN_001")
                .build();

        pendingPayment = Payment.builder()
                .paymentId(2L)
                .bookingId(20L)
                .userId(100L)
                .amount(200.0)
                .status(PaymentStatus.PENDING)
                .mode(PaymentMode.CASH)
                .transactionId("TXN_002")
                .build();
    }

    @Test
    void getPaymentById_existingId_returnsPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(paidPayment));

        Payment result = paymentService.getPaymentById(1L);

        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void getPaymentById_unknownId_throwsException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPaymentByBookingId_found_returnsPayment() {
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.of(paidPayment));

        Payment result = paymentService.getPaymentByBookingId(10L);

        assertThat(result.getBookingId()).isEqualTo(10L);
    }

    @Test
    void getPaymentByBookingId_notFound_throwsException() {
        when(paymentRepository.findByBookingId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByBookingId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPaymentsByUser_returnsUserPayments() {
        when(paymentRepository.findByUserId(100L)).thenReturn(List.of(paidPayment));

        List<Payment> result = paymentService.getPaymentsByUser(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(100L);
    }

    @Test
    void refundPayment_paidPayment_validAmount_setsRefundedStatus() {
        RefundRequest request = new RefundRequest();
        request.setAmount(100.0);
        request.setReason("Customer request");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(paidPayment));
        when(paymentRepository.save(paidPayment)).thenReturn(paidPayment);

        Payment result = paymentService.refundPayment(1L, request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.getRefundAmount()).isEqualTo(100.0);
    }

    @Test
    void refundPayment_notPaidPayment_throwsException() {
        RefundRequest request = new RefundRequest();
        request.setAmount(100.0);

        when(paymentRepository.findById(2L)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(2L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PAID payments");
    }

    @Test
    void refundPayment_amountExceedsPayment_throwsException() {
        RefundRequest request = new RefundRequest();
        request.setAmount(500.0);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(paidPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exceeds payment amount");
    }

    @Test
    void generateReceipt_paidPayment_returnsBytes() {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(paidPayment));
        when(receiptService.generate(paidPayment)).thenReturn(pdfBytes);

        byte[] result = paymentService.generateReceipt(1L);

        assertThat(result).isEqualTo(pdfBytes);
    }

    @Test
    void generateReceipt_nonPaidPayment_throwsException() {
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.generateReceipt(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAID payments");
    }
}
