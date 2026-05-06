package com.parkease.payment.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.parkease.payment.entity.Payment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ReceiptService {

    public byte[] generate(Payment payment) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph("PAYMENT RECEIPT")
                    .setBold()
                    .setFontSize(18));

            document.add(new Paragraph("Transaction ID: " + payment.getTransactionId()));
            document.add(new Paragraph("Booking ID: " + payment.getBookingId()));
            document.add(new Paragraph("User ID: " + payment.getUserId()));
            document.add(new Paragraph("Amount: INR " + payment.getAmount()));
            document.add(new Paragraph("Status: " + payment.getStatus()));

            if(payment.getMode() != null) {
                document.add(new Paragraph("Payment Mode: " + payment.getMode().name()));
            }

            if(payment.getPaidAt() != null) {
                document.add(new Paragraph("Paid At: " + payment.getPaidAt().toString()));
            }

            if(payment.getRefundAmount() != null) {
                document.add(new Paragraph("Refund Amount: INR " + payment.getRefundAmount()));
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate receipt", e);
        }
    }
}