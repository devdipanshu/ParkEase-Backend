package com.parkease.parkinglot.controller;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import com.parkease.parkinglot.entity.ParkingLot;
import com.parkease.parkinglot.service.ParkingLotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lots")
@RequiredArgsConstructor
public class ParkingLotResource {

    private final ParkingLotService parkingLotService;

    @PostMapping
    public ResponseEntity<ParkingLot> createLot(@Valid @RequestBody ParkingLotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingLotService.createLot(dto));
    }

    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAllApprovedLots() {
        return ResponseEntity.ok(parkingLotService.getAllApprovedLots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLot> getLotById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.getLotById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingLot>> getNearbyLots(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5.0") Double radius) {
        return ResponseEntity.ok(parkingLotService.getNearbyLots(lat, lng, radius));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<ParkingLot>> getLotsByCity(@PathVariable String city) {
        return ResponseEntity.ok(parkingLotService.getLotsByCity(city));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<ParkingLot>> getLotsByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(parkingLotService.getLotsByManager(managerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingLot> updateLot(@PathVariable Long id,
                                                 @Valid @RequestBody ParkingLotDTO dto) {
        return ResponseEntity.ok(parkingLotService.updateLot(id, dto));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ParkingLot> approveLot(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.approveLot(id));
    }
    @GetMapping("/pending")
    public ResponseEntity<List<ParkingLot>> getPendingLots() {
        return ResponseEntity.ok(parkingLotService.getPendingLots());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ParkingLot> toggleOpen(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.toggleOpen(id));
    }

    @PutMapping("/{id}/sync-spots")
    public ResponseEntity<ParkingLot> syncSpotCount(@PathVariable Long id,
                                                     @RequestParam int totalSpots) {
        return ResponseEntity.ok(parkingLotService.syncSpotCount(id, totalSpots));
    }

    @PutMapping("/{id}/decrement")
    public ResponseEntity<ParkingLot> decrementAvailable(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.decrementAvailable(id));
    }

    @PutMapping("/{id}/increment")
    public ResponseEntity<ParkingLot> incrementAvailable(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.incrementAvailable(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        parkingLotService.deleteLot(id);
        return ResponseEntity.noContent().build();
    }
}
