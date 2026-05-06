package com.parkease.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DailyReportResponse {

    private String reportDate;
    private Long lotId;
    private String lotName;
    private Double avgOccupancy;
    private Double peakOccupancy;
    private Integer peakHour;
    private Double totalRevenue;
    private Integer totalBookings;
    private List<PeakHourResponse> hourlyBreakdown;
    private List<VehicleTypeStatsResponse> vehicleTypeBreakdown;
    private LocalDateTime generatedAt;

}
