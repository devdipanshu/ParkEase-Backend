package com.parkease.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyTrendResponse {

    private String date;
    private Double avgOccupancy;
    private Integer minAvailableSpots;
    private Integer maxTotalSpots;
}
