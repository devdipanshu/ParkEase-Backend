package com.parkease.spot.service;

import com.parkease.spot.dto.SpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import com.parkease.spot.enums.SpotStatus;
import com.parkease.spot.enums.SpotType;
import com.parkease.spot.exception.ResourceNotFoundException;
import com.parkease.spot.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepository;

    @Override
    public ParkingSpot addSpot(SpotDTO dto) {
        ParkingSpot spot = buildSpot(dto);
        log.info("Adding spot {} to lot {}", dto.getSpotNumber(), dto.getLotId());
        return spotRepository.save(spot);
    }

    @Override
    public List<ParkingSpot> addBulkSpots(List<SpotDTO> spots) {
        List<ParkingSpot> entities = spots.stream()
                .map(this::buildSpot)
                .collect(Collectors.toList());
        log.info("Bulk adding {} spots to lot {}", entities.size(), spots.get(0).getLotId());
        return spotRepository.saveAll(entities);
    }

    @Override
    public ParkingSpot getSpotById(Long id) {
        return findOrThrow(id);
    }

    @Override
    public List<ParkingSpot> getSpotsByLot(Long lotId) {
        return spotRepository.findByLotId(lotId);
    }

    @Override
    public List<ParkingSpot> getAvailableSpots(Long lotId) {
        return spotRepository.findByLotIdAndStatus(lotId, SpotStatus.AVAILABLE);
    }

    @Override
    public List<ParkingSpot> getSpotsByType(Long lotId, SpotType type) {
        return spotRepository.findByLotIdAndSpotType(lotId, type);
    }

    @Override
    public Long countAvailable(Long lotId) {
        return spotRepository.countByLotIdAndStatus(lotId, SpotStatus.AVAILABLE);
    }

    @Override
    @Transactional
    public ParkingSpot occupySpot(Long id) {
        ParkingSpot spot = findOrThrow(id);
        if (spot.getStatus() != SpotStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Spot " + spot.getSpotNumber() + " is not available (current status: " + spot.getStatus() + ")");
        }
        spot.setStatus(SpotStatus.RESERVED);
        log.info("Spot {} reserved (AVAILABLE → RESERVED)", spot.getSpotNumber());
        return spotRepository.save(spot);
    }

    @Override
    @Transactional
    public ParkingSpot reserveToOccupied(Long id) {
        ParkingSpot spot = findOrThrow(id);
        if (spot.getStatus() == SpotStatus.OCCUPIED) {
            log.warn("Spot {} already OCCUPIED — treating check-in as idempotent", spot.getSpotNumber());
            return spot;
        }
        if (spot.getStatus() != SpotStatus.RESERVED) {
            throw new IllegalStateException(
                    "Spot " + spot.getSpotNumber() + " cannot be checked in (current status: " + spot.getStatus() + ")");
        }
        spot.setStatus(SpotStatus.OCCUPIED);
        log.info("Spot {} checked in (RESERVED → OCCUPIED)", spot.getSpotNumber());
        return spotRepository.save(spot);
    }

    @Override
    @Transactional
    public ParkingSpot releaseSpot(Long id) {
        ParkingSpot spot = findOrThrow(id);
        if (spot.getStatus() == SpotStatus.AVAILABLE) {
            log.warn("Spot {} already AVAILABLE — treating release as idempotent", spot.getSpotNumber());
            return spot;
        }
        spot.setStatus(SpotStatus.AVAILABLE);
        log.info("Spot {} released (→ AVAILABLE)", spot.getSpotNumber());
        return spotRepository.save(spot);
    }

    @Override
    public ParkingSpot updateSpot(Long id, SpotDTO dto) {
        ParkingSpot spot = findOrThrow(id);
        spot.setSpotNumber(dto.getSpotNumber());
        spot.setFloor(dto.getFloor());
        spot.setSpotType(dto.getSpotType());
        spot.setVehicleType(dto.getVehicleType());
        spot.setPricePerHour(dto.getPricePerHour());
        if (dto.getIsHandicapped() != null) spot.setIsHandicapped(dto.getIsHandicapped());
        if (dto.getIsEVCharging() != null) spot.setIsEVCharging(dto.getIsEVCharging());
        log.info("Updating spot id: {}", id);
        return spotRepository.save(spot);
    }

    @Override
    public void deleteSpot(Long id) {
        findOrThrow(id);
        log.info("Deleting spot id: {}", id);
        spotRepository.deleteById(id);
    }

    private ParkingSpot buildSpot(SpotDTO dto) {
        return ParkingSpot.builder()
                .lotId(dto.getLotId())
                .spotNumber(dto.getSpotNumber().toUpperCase())
                .floor(dto.getFloor())
                .spotType(dto.getSpotType())
                .vehicleType(dto.getVehicleType())
                .pricePerHour(dto.getPricePerHour())
                .isHandicapped(dto.getIsHandicapped() != null ? dto.getIsHandicapped() : false)
                .isEVCharging(dto.getIsEVCharging() != null ? dto.getIsEVCharging() : false)
                .status(SpotStatus.AVAILABLE)
                .build();
    }

    private ParkingSpot findOrThrow(Long id) {
        return spotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found with id: " + id));
    }
}
