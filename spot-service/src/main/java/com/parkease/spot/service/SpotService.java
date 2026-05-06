package com.parkease.spot.service;

import com.parkease.spot.dto.SpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import com.parkease.spot.enums.SpotType;

import java.util.List;

public interface SpotService {

    ParkingSpot addSpot(SpotDTO dto);

    List<ParkingSpot> addBulkSpots(List<SpotDTO> spots);

    ParkingSpot getSpotById(Long id);

    List<ParkingSpot> getSpotsByLot(Long lotId);

    List<ParkingSpot> getAvailableSpots(Long lotId);

    List<ParkingSpot> getSpotsByType(Long lotId, SpotType type);

    Long countAvailable(Long lotId);

    ParkingSpot occupySpot(Long id);

    ParkingSpot reserveToOccupied(Long id);

    ParkingSpot releaseSpot(Long id);

    ParkingSpot updateSpot(Long id, SpotDTO dto);

    void deleteSpot(Long id);
}
