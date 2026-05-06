package com.parkease.parkinglot.service;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import com.parkease.parkinglot.entity.ParkingLot;

import java.util.List;

public interface ParkingLotService {

    ParkingLot createLot(ParkingLotDTO dto);

    ParkingLot getLotById(Long id);

    List<ParkingLot> getNearbyLots(Double lat, Double lng, Double radius);

    List<ParkingLot> getLotsByCity(String city);

    List<ParkingLot> getAllApprovedLots();

    List<ParkingLot> getLotsByManager(Long managerId);

    ParkingLot updateLot(Long id, ParkingLotDTO dto);

    ParkingLot toggleOpen(Long id);

    ParkingLot approveLot(Long id);

    ParkingLot decrementAvailable(Long id);

    ParkingLot incrementAvailable(Long id);

    ParkingLot syncSpotCount(Long id, int totalSpots);
    public List<ParkingLot> getPendingLots();

    void deleteLot(Long id);
}
