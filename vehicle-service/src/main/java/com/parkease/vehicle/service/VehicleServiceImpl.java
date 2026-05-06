package com.parkease.vehicle.service;

import com.parkease.vehicle.dto.RegisterVehicleRequest;
import com.parkease.vehicle.dto.UpdateVehicleRequest;
import com.parkease.vehicle.dto.VehicleResponse;
import com.parkease.vehicle.entity.Vehicle;
import com.parkease.vehicle.entity.VehicleType;
import com.parkease.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleResponse registerVehicle(RegisterVehicleRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new IllegalArgumentException("Vehicle with license plate '" + request.getLicensePlate() + "' already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .ownerId(request.getOwnerId())
                .licensePlate(request.getLicensePlate().toUpperCase())
                .make(request.getMake())
                .model(request.getModel())
                .color(request.getColor())
                .vehicleType(request.getVehicleType())
                .isEV(request.isEV())
                .build();

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse getVehicleById(Long vehicleId) {
        Vehicle vehicle = findOrThrow(vehicleId);
        return VehicleResponse.from(vehicle);
    }

    @Override
    public List<VehicleResponse> getVehiclesByOwner(Long ownerId) {
        return vehicleRepository.findByOwnerId(ownerId)
                .stream()
                .map(VehicleResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getByLicensePlate(String licensePlate) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("No vehicle found with license plate: " + licensePlate));
        return VehicleResponse.from(vehicle);
    }

    @Override
    public VehicleResponse updateVehicle(Long vehicleId, UpdateVehicleRequest request) {
        Vehicle vehicle = findOrThrow(vehicleId);

        if (request.getMake() != null) vehicle.setMake(request.getMake());
        if (request.getModel() != null) vehicle.setModel(request.getModel());
        if (request.getColor() != null) vehicle.setColor(request.getColor());
        if (request.getVehicleType() != null) vehicle.setVehicleType(request.getVehicleType());
        if (request.getIsEV() != null) vehicle.setEV(request.getIsEV());
        if (request.getIsActive() != null) vehicle.setActive(request.getIsActive());

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    public void deleteVehicle(Long vehicleId) {
        findOrThrow(vehicleId);
        vehicleRepository.deleteById(vehicleId);
    }

    @Override
    public VehicleType getVehicleType(Long vehicleId) {
        return findOrThrow(vehicleId).getVehicleType();
    }

    @Override
    public boolean isEVVehicle(Long vehicleId) {
        return findOrThrow(vehicleId).isEV();
    }

    private Vehicle findOrThrow(Long vehicleId) {
        return vehicleRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + vehicleId));
    }
}
