package com.parkease.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PeakHourResponse {

    private Integer hour;
    private String hourLabel;
    private Double avgOccupancy;
    private Long logCount;
    private Boolean isPeak;
}
