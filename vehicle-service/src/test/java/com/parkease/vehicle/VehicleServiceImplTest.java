package com.parkease.vehicle;

import com.parkease.vehicle.dto.RegisterVehicleRequest;
import com.parkease.vehicle.dto.UpdateVehicleRequest;
import com.parkease.vehicle.dto.VehicleResponse;
import com.parkease.vehicle.entity.Vehicle;
import com.parkease.vehicle.entity.VehicleType;
import com.parkease.vehicle.repository.VehicleRepository;
import com.parkease.vehicle.service.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .vehicleId(1L)
                .ownerId(10L)
                .licensePlate("MH12AB1234")
                .make("Toyota")
                .model("Corolla")
                .color("White")
                .vehicleType(VehicleType.CAR)
                .isEV(false)
                .registeredAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    @Test
    void registerVehicle_newPlate_savesVehicle() {
        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setOwnerId(10L);
        request.setLicensePlate("mh12ab1234");
        request.setMake("Toyota");
        request.setModel("Corolla");
        request.setColor("White");
        request.setVehicleType(VehicleType.CAR);
        request.setEV(false);

        when(vehicleRepository.existsByLicensePlate("mh12ab1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.registerVehicle(request);

        assertThat(response.getLicensePlate()).isEqualTo("MH12AB1234");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void registerVehicle_duplicatePlate_throwsException() {
        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setLicensePlate("MH12AB1234");

        when(vehicleRepository.existsByLicensePlate("MH12AB1234")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.registerVehicle(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getVehicleById_existingId_returnsResponse() {
        when(vehicleRepository.findByVehicleId(1L)).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById(1L);

        assertThat(response.getVehicleId()).isEqualTo(1L);
        assertThat(response.getMake()).isEqualTo("Toyota");
    }

    @Test
    void getVehicleById_unknownId_throwsException() {
        when(vehicleRepository.findByVehicleId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void getVehiclesByOwner_returnsMatchingVehicles() {
        when(vehicleRepository.findByOwnerId(10L)).thenReturn(List.of(vehicle));

        List<VehicleResponse> result = vehicleService.getVehiclesByOwner(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwnerId()).isEqualTo(10L);
    }

    @Test
    void getByLicensePlate_found_returnsResponse() {
        when(vehicleRepository.findByLicensePlate("MH12AB1234")).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getByLicensePlate("mh12ab1234");

        assertThat(response.getLicensePlate()).isEqualTo("MH12AB1234");
    }

    @Test
    void updateVehicle_partialUpdate_onlyChangesProvidedFields() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setColor("Black");

        when(vehicleRepository.findByVehicleId(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.updateVehicle(1L, request);

        assertThat(vehicle.getColor()).isEqualTo("Black");
        assertThat(vehicle.getMake()).isEqualTo("Toyota");
    }

    @Test
    void deleteVehicle_existingId_callsDelete() {
        when(vehicleRepository.findByVehicleId(1L)).thenReturn(Optional.of(vehicle));

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void getVehicleType_returnsCorrectType() {
        when(vehicleRepository.findByVehicleId(1L)).thenReturn(Optional.of(vehicle));

        VehicleType type = vehicleService.getVehicleType(1L);

        assertThat(type).isEqualTo(VehicleType.CAR);
    }

    @Test
    void isEVVehicle_nonEV_returnsFalse() {
        when(vehicleRepository.findByVehicleId(1L)).thenReturn(Optional.of(vehicle));

        assertThat(vehicleService.isEVVehicle(1L)).isFalse();
    }
}
