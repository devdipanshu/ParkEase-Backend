package com.parkease.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RevenueStatsResponse {

    private Long lotId;
    private Double totalRevenue;
    private Integer transactionCount;
    private Double averageTransactionAmount;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
