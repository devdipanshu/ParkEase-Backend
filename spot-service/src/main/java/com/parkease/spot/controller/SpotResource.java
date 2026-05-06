package com.parkease.spot.controller;

import com.parkease.spot.dto.BulkSpotRequest;
import com.parkease.spot.dto.SpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import com.parkease.spot.enums.SpotType;
import com.parkease.spot.service.SpotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spots")
@RequiredArgsConstructor
public class SpotResource {

    private final SpotService spotService;

    @PostMapping
    public ResponseEntity<ParkingSpot> addSpot(@Valid @RequestBody SpotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spotService.addSpot(dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ParkingSpot>> addBulkSpots(@Valid @RequestBody BulkSpotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spotService.addBulkSpots(request.getSpots()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpot> getSpotById(@PathVariable Long id) {
        return ResponseEntity.ok(spotService.getSpotById(id));
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<ParkingSpot>> getSpotsByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(spotService.getSpotsByLot(lotId));
    }

    @GetMapping("/lot/{lotId}/available")
    public ResponseEntity<List<ParkingSpot>> getAvailableSpots(@PathVariable Long lotId) {
        return ResponseEntity.ok(spotService.getAvailableSpots(lotId));
    }

    @GetMapping("/lot/{lotId}/type/{type}")
    public ResponseEntity<List<ParkingSpot>> getSpotsByType(@PathVariable Long lotId,
                                                             @PathVariable SpotType type) {
        return ResponseEntity.ok(spotService.getSpotsByType(lotId, type));
    }

    @GetMapping("/lot/{lotId}/count")
    public ResponseEntity<Long> countAvailable(@PathVariable Long lotId) {
        return ResponseEntity.ok(spotService.countAvailable(lotId));
    }

    @PutMapping("/{id}/occupy")
    public ResponseEntity<ParkingSpot> occupySpot(@PathVariable Long id) {
        return ResponseEntity.ok(spotService.occupySpot(id));
    }

    @PutMapping("/{id}/checkin")
    public ResponseEntity<ParkingSpot> reserveToOccupied(@PathVariable Long id) {
        return ResponseEntity.ok(spotService.reserveToOccupied(id));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<ParkingSpot> releaseSpot(@PathVariable Long id) {
        return ResponseEntity.ok(spotService.releaseSpot(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingSpot> updateSpot(@PathVariable Long id,
                                                   @Valid @RequestBody SpotDTO dto) {
        return ResponseEntity.ok(spotService.updateSpot(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpot(@PathVariable Long id) {
        spotService.deleteSpot(id);
        return ResponseEntity.noContent().build();
    }
}
