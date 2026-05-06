package com.parkease.spot.repository;

import com.parkease.spot.entity.ParkingSpot;
import com.parkease.spot.enums.SpotStatus;
import com.parkease.spot.enums.SpotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<ParkingSpot, Long> {

    List<ParkingSpot> findByLotId(Long lotId);

    List<ParkingSpot> findByLotIdAndStatus(Long lotId, SpotStatus status);

    List<ParkingSpot> findByLotIdAndSpotType(Long lotId, SpotType spotType);

    Long countByLotIdAndStatus(Long lotId, SpotStatus status);
}
