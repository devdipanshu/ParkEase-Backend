package com.parkease.vehicle.dto;

import com.parkease.vehicle.entity.VehicleType;
import lombok.Data;

@Data
public class UpdateVehicleRequest {

    private String make;
    private String model;
    private String color;
    private VehicleType vehicleType;
    private Boolean isEV;
    private Boolean isActive;
}
