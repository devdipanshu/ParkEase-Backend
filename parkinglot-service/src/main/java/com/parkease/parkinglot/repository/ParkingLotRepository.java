package com.parkease.parkinglot.repository;

import com.parkease.parkinglot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    List<ParkingLot> findByCity(String city);

    List<ParkingLot> findByManagerId(Long managerId);

    List<ParkingLot> findByIsApprovedTrue();

    List<ParkingLot> findByIsOpenTrue();

    List<ParkingLot> findByIsApprovedFalse();

    @Query(value = "SELECT * FROM parking_lots p WHERE p.is_approved = true AND " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
            "cos(radians(p.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(p.latitude)))) <= :radius",
            nativeQuery = true)
    List<ParkingLot> findNearby(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius);
}
