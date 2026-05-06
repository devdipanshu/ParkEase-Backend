package com.parkease.vehicle.service;

import com.parkease.vehicle.dto.RegisterVehicleRequest;
import com.parkease.vehicle.dto.UpdateVehicleRequest;
import com.parkease.vehicle.dto.VehicleResponse;
import com.parkease.vehicle.entity.VehicleType;

import java.util.List;

public interface VehicleService {

    VehicleResponse registerVehicle(RegisterVehicleRequest request);

    VehicleResponse getVehicleById(Long vehicleId);

    List<VehicleResponse> getVehiclesByOwner(Long ownerId);

    VehicleResponse getByLicensePlate(String licensePlate);

    VehicleResponse updateVehicle(Long vehicleId, UpdateVehicleRequest request);

    void deleteVehicle(Long vehicleId);

    VehicleType getVehicleType(Long vehicleId);

    boolean isEVVehicle(Long vehicleId);
}
