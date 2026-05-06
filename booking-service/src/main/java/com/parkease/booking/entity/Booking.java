package com.parkease.booking.entity;

import com.parkease.booking.enums.BookingStatus;
import com.parkease.booking.enums.BookingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long lotId;

    @Column(nullable = false)
    private Long spotId;

    private String vehiclePlate;

    private String vehicleType;

    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime actualCheckInTime;

    private LocalDateTime actualCheckOutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.RESERVED;

    private Double pricePerHour;

    private Double totalAmount;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
