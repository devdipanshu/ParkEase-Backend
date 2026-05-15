package com.parkease.parkinglot.service;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import com.parkease.parkinglot.entity.ParkingLot;
import com.parkease.parkinglot.exception.ResourceNotFoundException;
import com.parkease.parkinglot.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public ParkingLot createLot(ParkingLotDTO dto) {
        ParkingLot lot = ParkingLot.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .totalSpots(dto.getTotalSpots())
                .availableSpots(dto.getTotalSpots())
                .managerId(dto.getManagerId())
                .isOpen(dto.getIsOpen() != null ? dto.getIsOpen() : true)
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .isApproved(false)
                .imageUrl(dto.getImageUrl())
                .build();
        log.info("Creating parking lot: {}", lot.getName());
        return parkingLotRepository.save(lot);
    }

    @Override
    @Cacheable(value = "lots", key = "#id")
    public ParkingLot getLotById(Long id) {
        return findOrThrow(id);
    }

    @Override
    public List<ParkingLot> getNearbyLots(Double lat, Double lng, Double radius) {
        return parkingLotRepository.findNearby(lat, lng, radius);
    }

    @Override
    public List<ParkingLot> getLotsByCity(String city) {
        return parkingLotRepository.findByCity(city);
    }

    @Override
    @Cacheable(value = "lots-approved")
    public List<ParkingLot> getAllApprovedLots() {
        return parkingLotRepository.findByIsApprovedTrue();
    }

    @Override
    public List<ParkingLot> getPendingLots() {
        return parkingLotRepository.findByIsApprovedFalse();
    }

    @Override
    public List<ParkingLot> getLotsByManager(Long managerId) {
        return parkingLotRepository.findByManagerId(managerId);
    }

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public ParkingLot updateLot(Long id, ParkingLotDTO dto) {
        ParkingLot lot = findOrThrow(id);
        lot.setName(dto.getName());
        lot.setAddress(dto.getAddress());
        lot.setCity(dto.getCity());
        lot.setLatitude(dto.getLatitude());
        lot.setLongitude(dto.getLongitude());
        lot.setManagerId(dto.getManagerId());
        lot.setOpenTime(dto.getOpenTime());
        lot.setCloseTime(dto.getCloseTime());
        lot.setImageUrl(dto.getImageUrl());
        if (dto.getIsOpen() != null) {
            lot.setIsOpen(dto.getIsOpen());
        }
        if (dto.getTotalSpots() != null && !dto.getTotalSpots().equals(lot.getTotalSpots())) {
            int diff = dto.getTotalSpots() - lot.getTotalSpots();
            lot.setTotalSpots(dto.getTotalSpots());
            lot.setAvailableSpots(Math.max(0, lot.getAvailableSpots() + diff));
        }
        log.info("Updating parking lot id: {}", id);
        return parkingLotRepository.save(lot);
    }

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public ParkingLot toggleOpen(Long id) {
        ParkingLot lot = findOrThrow(id);
        lot.setIsOpen(!lot.getIsOpen());
        log.info("Toggling lot id: {} open status to: {}", id, lot.getIsOpen());
        return parkingLotRepository.save(lot);
    }

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public ParkingLot approveLot(Long id) {
        ParkingLot lot = findOrThrow(id);
        lot.setIsApproved(true);
        log.info("Approving parking lot id: {}", id);
        return parkingLotRepository.save(lot);
    }

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public ParkingLot decrementAvailable(Long id) {
        ParkingLot lot = findOrThrow(id);
        if (lot.getAvailableSpots() <= 0) {
            throw new IllegalStateException("No available spots remaining in lot id: " + id);
        }
        lot.setAvailableSpots(lot.getAvailableSpots() - 1);
        return parkingLotRepository.save(lot);
    }

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public ParkingLot incrementAvailable(Long id) {
        ParkingLot lot = findOrThrow(id);
        lot.setAvailableSpots(lot.getAvailableSpots() + 1);
        if (lot.getAvailableSpots() > lot.getTotalSpots()) {
            lot.setTotalSpots(lot.getAvailableSpots());
        }
        return parkingLotRepository.save(lot);
    }

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public ParkingLot syncSpotCount(Long id, int totalSpots) {
        ParkingLot lot = findOrThrow(id);
        int diff = totalSpots - lot.getTotalSpots();
        lot.setTotalSpots(totalSpots);
        lot.setAvailableSpots(Math.max(0, lot.getAvailableSpots() + diff));
        log.info("Syncing lot {} totalSpots to {}, availableSpots to {}", id, totalSpots, lot.getAvailableSpots());
        return parkingLotRepository.save(lot);
    }

    @Override
    @CacheEvict(value = {"lots", "lots-approved"}, allEntries = true)
    public void deleteLot(Long id) {
        findOrThrow(id);
        log.info("Deleting parking lot id: {}", id);
        parkingLotRepository.deleteById(id);
    }

    private ParkingLot findOrThrow(Long id) {
        return parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));
    }
}
