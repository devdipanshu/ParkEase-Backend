package com.parkease.parkinglot;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import com.parkease.parkinglot.entity.ParkingLot;
import com.parkease.parkinglot.exception.ResourceNotFoundException;
import com.parkease.parkinglot.repository.ParkingLotRepository;
import com.parkease.parkinglot.service.ParkingLotServiceImpl;
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
class ParkingLotServiceImplTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @InjectMocks
    private ParkingLotServiceImpl parkingLotService;

    private ParkingLot lot;

    @BeforeEach
    void setUp() {
        lot = ParkingLot.builder()
                .lotId(1L)
                .name("Central Parking")
                .city("Mumbai")
                .latitude(19.0760)
                .longitude(72.8777)
                .totalSpots(100)
                .availableSpots(100)
                .managerId(5L)
                .isOpen(true)
                .isApproved(false)
                .build();
    }

    @Test
    void createLot_savesWithDefaultApprovedFalse() {
        ParkingLotDTO dto = new ParkingLotDTO();
        dto.setName("Central Parking");
        dto.setCity("Mumbai");
        dto.setLatitude(19.0760);
        dto.setLongitude(72.8777);
        dto.setTotalSpots(100);
        dto.setManagerId(5L);

        when(parkingLotRepository.save(any(ParkingLot.class))).thenReturn(lot);

        ParkingLot result = parkingLotService.createLot(dto);

        assertThat(result.getName()).isEqualTo("Central Parking");
        assertThat(result.getIsApproved()).isFalse();
        verify(parkingLotRepository).save(any(ParkingLot.class));
    }

    @Test
    void getLotById_existingId_returnsLot() {
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(lot));

        ParkingLot result = parkingLotService.getLotById(1L);

        assertThat(result.getLotId()).isEqualTo(1L);
    }

    @Test
    void getLotById_unknownId_throwsException() {
        when(parkingLotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingLotService.getLotById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void approveLot_setsIsApprovedTrue() {
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(parkingLotRepository.save(lot)).thenReturn(lot);

        ParkingLot result = parkingLotService.approveLot(1L);

        assertThat(result.getIsApproved()).isTrue();
    }

    @Test
    void toggleOpen_opensClosedLot() {
        lot.setIsOpen(false);
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(parkingLotRepository.save(lot)).thenReturn(lot);

        ParkingLot result = parkingLotService.toggleOpen(1L);

        assertThat(result.getIsOpen()).isTrue();
    }

    @Test
    void decrementAvailable_withSpotsRemaining_decrementsCount() {
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(parkingLotRepository.save(lot)).thenReturn(lot);

        ParkingLot result = parkingLotService.decrementAvailable(1L);

        assertThat(result.getAvailableSpots()).isEqualTo(99);
    }

    @Test
    void decrementAvailable_noSpotsLeft_throwsException() {
        lot.setAvailableSpots(0);
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(lot));

        assertThatThrownBy(() -> parkingLotService.decrementAvailable(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No available spots");
    }

    @Test
    void incrementAvailable_incrementsCount() {
        lot.setAvailableSpots(50);
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(parkingLotRepository.save(lot)).thenReturn(lot);

        ParkingLot result = parkingLotService.incrementAvailable(1L);

        assertThat(result.getAvailableSpots()).isEqualTo(51);
    }

    @Test
    void getLotsByManager_returnsManagerLots() {
        when(parkingLotRepository.findByManagerId(5L)).thenReturn(List.of(lot));

        List<ParkingLot> result = parkingLotService.getLotsByManager(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getManagerId()).isEqualTo(5L);
    }

    @Test
    void deleteLot_callsRepositoryDelete() {
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(lot));

        parkingLotService.deleteLot(1L);

        verify(parkingLotRepository).deleteById(1L);
    }
}
