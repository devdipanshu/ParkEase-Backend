package com.parkease.vehicle.resource;

import com.parkease.vehicle.dto.RegisterVehicleRequest;
import com.parkease.vehicle.dto.UpdateVehicleRequest;
import com.parkease.vehicle.dto.VehicleResponse;
import com.parkease.vehicle.entity.VehicleType;
import com.parkease.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleResource {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> registerVehicle(@Valid @RequestBody RegisterVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.registerVehicle(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByOwner(ownerId));
    }

    @GetMapping("/plate/{licensePlate}")
    public ResponseEntity<VehicleResponse> getByLicensePlate(@PathVariable String licensePlate) {
        return ResponseEntity.ok(vehicleService.getByLicensePlate(licensePlate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable Long id,
                                                          @RequestBody UpdateVehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/type")
    public ResponseEntity<VehicleType> getVehicleType(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleType(id));
    }

    @GetMapping("/{id}/ev")
    public ResponseEntity<Boolean> isEVVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.isEVVehicle(id));
    }
}
