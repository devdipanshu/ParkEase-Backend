package com.parkease.parkinglot.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ParkingLotDTO {

    @NotBlank(message = "Lot name is required")
    private String name;

    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @NotNull(message = "Total spots is required")
    @Min(value = 1, message = "Total spots must be at least 1")
    private Integer totalSpots;

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    private Boolean isOpen;

    private LocalTime openTime;

    private LocalTime closeTime;

    private String imageUrl;
}
