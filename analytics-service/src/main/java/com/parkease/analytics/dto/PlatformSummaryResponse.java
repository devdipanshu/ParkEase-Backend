package com.parkease.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlatformSummaryResponse {

    private Integer totalActiveLots;
    private Integer totalSpots;
    private Integer totalAvailableSpots;
    private Double platformOccupancyRate;
    private Double totalRevenueToday;
    private Double totalRevenueThisMonth;
    private Integer totalBookingsToday;
    private Integer totalBookingsThisMonth;
    private String peakHourToday;
    private LocalDateTime generatedAt;
}
