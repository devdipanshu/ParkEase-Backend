package com.parkease.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OccupancyResponse {

    private Long lotId;
    private Double averageOccupancy;
    private Double currentOccupancy;
    private Integer totalLogs;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
