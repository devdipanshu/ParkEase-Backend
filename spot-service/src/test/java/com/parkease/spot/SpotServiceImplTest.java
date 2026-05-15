package com.parkease.spot;

import com.parkease.spot.dto.SpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import com.parkease.spot.enums.SpotStatus;
import com.parkease.spot.enums.SpotType;
import com.parkease.spot.enums.VehicleType;
import com.parkease.spot.exception.ResourceNotFoundException;
import com.parkease.spot.repository.SpotRepository;
import com.parkease.spot.service.SpotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotServiceImplTest {

    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private SpotServiceImpl spotService;

    private ParkingSpot spot;

    @BeforeEach
    void setUp() {
        spot = ParkingSpot.builder()
                .spotId(1L)
                .lotId(10L)
                .spotNumber("A01")
                .floor(1)
                .spotType(SpotType.STANDARD)
                .vehicleType(VehicleType.FOUR_WHEELER)
                .status(SpotStatus.AVAILABLE)
                .pricePerHour(50.0)
                .isHandicapped(false)
                .isEVCharging(false)
                .build();
    }

    @Test
    void addSpot_savesAndReturnsSpot() {
        SpotDTO dto = buildSpotDTO();
        when(spotRepository.save(any(ParkingSpot.class))).thenReturn(spot);

        ParkingSpot result = spotService.addSpot(dto);

        assertThat(result.getLotId()).isEqualTo(10L);
        verify(spotRepository).save(any(ParkingSpot.class));
    }

    @Test
    void getSpotById_existingId_returnsSpot() {
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        ParkingSpot result = spotService.getSpotById(1L);

        assertThat(result.getSpotId()).isEqualTo(1L);
    }

    @Test
    void getSpotById_unknownId_throwsException() {
        when(spotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spotService.getSpotById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void occupySpot_availableSpot_setsReservedStatus() {
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));
        when(spotRepository.save(spot)).thenReturn(spot);

        ParkingSpot result = spotService.occupySpot(1L);

        assertThat(result.getStatus()).isEqualTo(SpotStatus.RESERVED);
    }

    @Test
    void occupySpot_notAvailableSpot_throwsException() {
        spot.setStatus(SpotStatus.OCCUPIED);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        assertThatThrownBy(() -> spotService.occupySpot(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void reserveToOccupied_reservedSpot_setsOccupiedStatus() {
        spot.setStatus(SpotStatus.RESERVED);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));
        when(spotRepository.save(spot)).thenReturn(spot);

        ParkingSpot result = spotService.reserveToOccupied(1L);

        assertThat(result.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
    }

    @Test
    void reserveToOccupied_notReserved_throwsException() {
        spot.setStatus(SpotStatus.AVAILABLE);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        assertThatThrownBy(() -> spotService.reserveToOccupied(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in RESERVED state");
    }

    @Test
    void releaseSpot_occupiedSpot_setsAvailableStatus() {
        spot.setStatus(SpotStatus.OCCUPIED);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));
        when(spotRepository.save(spot)).thenReturn(spot);

        ParkingSpot result = spotService.releaseSpot(1L);

        assertThat(result.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
    }

    @Test
    void releaseSpot_alreadyAvailable_throwsException() {
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        assertThatThrownBy(() -> spotService.releaseSpot(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already AVAILABLE");
    }

    @Test
    void getAvailableSpots_returnsOnlyAvailable() {
        when(spotRepository.findByLotIdAndStatus(10L, SpotStatus.AVAILABLE)).thenReturn(List.of(spot));

        List<ParkingSpot> result = spotService.getAvailableSpots(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SpotStatus.AVAILABLE);
    }

    @Test
    void deleteSpot_existingId_callsDelete() {
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        spotService.deleteSpot(1L);

        verify(spotRepository).deleteById(1L);
    }

    private SpotDTO buildSpotDTO() {
        SpotDTO dto = new SpotDTO();
        dto.setLotId(10L);
        dto.setSpotNumber("a01");
        dto.setFloor(1);
        dto.setSpotType(SpotType.STANDARD);
        dto.setVehicleType(VehicleType.FOUR_WHEELER);
        dto.setPricePerHour(50.0);
        return dto;
    }
}
