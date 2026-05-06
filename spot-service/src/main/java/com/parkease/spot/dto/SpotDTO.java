package com.parkease.spot.dto;

import com.parkease.spot.enums.SpotType;
import com.parkease.spot.enums.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpotDTO {

    @NotNull(message = "Lot ID is required")
    private Long lotId;

    @NotBlank(message = "Spot number is required")
    private String spotNumber;

    @NotNull(message = "Floor is required")
    private Integer floor;

    @NotNull(message = "Spot type is required")
    private SpotType spotType;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Price per hour is required")
    @Min(value = 0, message = "Price per hour must be non-negative")
    private Double pricePerHour;

    private Boolean isHandicapped;

    private Boolean isEVCharging;
}
