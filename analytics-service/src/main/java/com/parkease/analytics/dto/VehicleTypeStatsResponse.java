package com.parkease.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleTypeStatsResponse {

    private String vehicleType;
    private Long count;
    private Double avgOccupancy;
    private Double percentage;
}
