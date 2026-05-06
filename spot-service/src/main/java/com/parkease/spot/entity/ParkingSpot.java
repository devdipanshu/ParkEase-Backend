package com.parkease.spot.entity;

import com.parkease.spot.enums.SpotStatus;
import com.parkease.spot.enums.SpotType;
import com.parkease.spot.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "parking_spots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"lotId", "spotNumber"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;

    @Column(nullable = false)
    private Long lotId;

    @Column(nullable = false)
    private String spotNumber;

    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotType spotType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SpotStatus status = SpotStatus.AVAILABLE;

    @Builder.Default
    private Boolean isHandicapped = false;

    @Builder.Default
    private Boolean isEVCharging = false;

    private Double pricePerHour;

    @Version
    private Long version;

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
