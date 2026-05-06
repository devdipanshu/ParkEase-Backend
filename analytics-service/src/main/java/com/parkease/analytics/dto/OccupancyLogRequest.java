package com.parkease.analytics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OccupancyLogRequest {

    @NotNull(message = "Lot ID is required")
    private Long lotId;

    private Long spotId;

    @NotNull(message = "Occupancy rate is required")
    private Double occupancyRate;

    @NotNull(message = "Available spots is required")
    private Integer availableSpots;

    @NotNull(message = "Total spots is required")
    private Integer totalSpots;

    private String vehicleType;

    private LocalDateTime timestamp;
}
