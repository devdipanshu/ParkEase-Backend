package com.parkease.vehicle.repository;

import com.parkease.vehicle.entity.Vehicle;
import com.parkease.vehicle.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByOwnerId(Long ownerId);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByVehicleType(VehicleType vehicleType);

    List<Vehicle> findByIsEV(boolean isEV);

    boolean existsByLicensePlate(String licensePlate);

    Optional<Vehicle> findByVehicleId(Long vehicleId);
}
