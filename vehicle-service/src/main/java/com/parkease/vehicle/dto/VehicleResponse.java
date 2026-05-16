package com.parkease.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parkease.vehicle.entity.Vehicle;
import com.parkease.vehicle.entity.VehicleType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VehicleResponse {

    private Long vehicleId;
    private Long ownerId;
    private String licensePlate;
    private String make;
    private String model;
    private String color;
    private VehicleType vehicleType;
    @JsonProperty("isEV")
    private boolean isEV;
    private LocalDateTime registeredAt;
    @JsonProperty("isActive")
    private boolean isActive;

    public static VehicleResponse from(Vehicle vehicle) {
        return VehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .ownerId(vehicle.getOwnerId())
                .licensePlate(vehicle.getLicensePlate())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .vehicleType(vehicle.getVehicleType())
                .isEV(vehicle.isEV())
                .registeredAt(vehicle.getRegisteredAt())
                .isActive(vehicle.isActive())
                .build();
    }
}
