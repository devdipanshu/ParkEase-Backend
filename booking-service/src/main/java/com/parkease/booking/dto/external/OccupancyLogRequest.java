package com.parkease.booking.dto.external;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OccupancyLogRequest {
    private Long lotId;
    private Long spotId;
    private Double occupancyRate;
    private Integer availableSpots;
    private Integer totalSpots;
    private String vehicleType;
    private LocalDateTime timestamp;
}
