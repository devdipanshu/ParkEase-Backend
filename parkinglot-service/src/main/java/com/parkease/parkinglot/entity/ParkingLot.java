package com.parkease.parkinglot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "parking_lots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLot implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lotId;

    @Column(nullable = false)
    private String name;

    private String address;

    private String city;

    private Double latitude;

    private Double longitude;

    private Integer totalSpots;

    private Integer availableSpots;

    private Long managerId;

    @Builder.Default
    private Boolean isOpen = true;

    private LocalTime openTime;

    private LocalTime closeTime;

    @Builder.Default
    private Boolean isApproved = false;

    private String imageUrl;

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
