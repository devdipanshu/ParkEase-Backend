package com.parkease.analytics;

import com.parkease.analytics.client.BookingClient;
import com.parkease.analytics.client.ParkingLotClient;
import com.parkease.analytics.client.PaymentClient;
import com.parkease.analytics.dto.OccupancyLogRequest;
import com.parkease.analytics.dto.OccupancyResponse;
import com.parkease.analytics.dto.VehicleTypeStatsResponse;
import com.parkease.analytics.entity.OccupancyLog;
import com.parkease.analytics.repository.AnalyticsRepository;
import com.parkease.analytics.service.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private BookingClient bookingClient;

    @Mock
    private ParkingLotClient parkingLotClient;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private OccupancyLog occupancyLog;

    @BeforeEach
    void setUp() {
        occupancyLog = OccupancyLog.builder()
                .logId(1L)
                .lotId(10L)
                .spotId(5L)
                .occupancyRate(75.0)
                .availableSpots(25)
                .totalSpots(100)
                .vehicleType("CAR")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void logOccupancy_savesAndReturnsLog() {
        OccupancyLogRequest request = new OccupancyLogRequest();
        request.setLotId(10L);
        request.setSpotId(5L);
        request.setOccupancyRate(75.0);
        request.setAvailableSpots(25);
        request.setTotalSpots(100);
        request.setVehicleType("CAR");
        request.setTimestamp(LocalDateTime.now());

        when(analyticsRepository.save(any(OccupancyLog.class))).thenReturn(occupancyLog);

        OccupancyLog result = analyticsService.logOccupancy(request);

        assertThat(result.getLotId()).isEqualTo(10L);
        assertThat(result.getOccupancyRate()).isEqualTo(75.0);
        verify(analyticsRepository).save(any(OccupancyLog.class));
    }

    @Test
    void getOccupancyRateBetween_withLogs_returnsAverageOccupancy() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();

        when(analyticsRepository.avgOccupancyByLotIdBetween(10L, start, end)).thenReturn(70.0);
        when(analyticsRepository.findByLotIdAndTimestampBetween(10L, start, end))
                .thenReturn(List.of(occupancyLog));

        OccupancyResponse response = analyticsService.getOccupancyRateBetween(10L, start, end);

        assertThat(response.getLotId()).isEqualTo(10L);
        assertThat(response.getAverageOccupancy()).isEqualTo(70.0);
        assertThat(response.getTotalLogs()).isEqualTo(1);
    }

    @Test
    void getOccupancyRateBetween_noLogs_returnsZeroOccupancy() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();

        when(analyticsRepository.avgOccupancyByLotIdBetween(10L, start, end)).thenReturn(null);
        when(analyticsRepository.findByLotIdAndTimestampBetween(10L, start, end))
                .thenReturn(Collections.emptyList());

        OccupancyResponse response = analyticsService.getOccupancyRateBetween(10L, start, end);

        assertThat(response.getAverageOccupancy()).isEqualTo(0.0);
        assertThat(response.getTotalLogs()).isEqualTo(0);
    }

    @Test
    void getVehicleTypeStats_noData_returnsEmptyList() {
        when(analyticsRepository.findVehicleTypeDistribution(10L)).thenReturn(Collections.emptyList());

        List<VehicleTypeStatsResponse> result = analyticsService.getVehicleTypeStats(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void getVehicleTypeStats_withData_computesPercentages() {
        Object[] carRow = new Object[]{"CAR", 80L, 65.0};
        Object[] motoRow = new Object[]{"MOTORCYCLE", 20L, 45.0};

        when(analyticsRepository.findVehicleTypeDistribution(10L)).thenReturn(List.of(carRow, motoRow));

        List<VehicleTypeStatsResponse> result = analyticsService.getVehicleTypeStats(10L);

        assertThat(result).hasSize(2);
        VehicleTypeStatsResponse carStats = result.stream()
                .filter(r -> "CAR".equals(r.getVehicleType()))
                .findFirst().orElseThrow();
        assertThat(carStats.getPercentage()).isEqualTo(80.0);
        assertThat(carStats.getCount()).isEqualTo(80L);
    }

    @Test
    void getOccupancyRate_parkingLotServiceUnavailable_fallsBackToLastLog() {
        when(analyticsRepository.findByLotId(10L)).thenReturn(List.of(occupancyLog));
        when(analyticsRepository.avgOccupancyByLotId(10L)).thenReturn(75.0);
        when(parkingLotClient.getLotById(10L)).thenThrow(new RuntimeException("Service unavailable"));

        OccupancyResponse response = analyticsService.getOccupancyRate(10L);

        assertThat(response.getLotId()).isEqualTo(10L);
        assertThat(response.getCurrentOccupancy()).isEqualTo(75.0);
    }
}
