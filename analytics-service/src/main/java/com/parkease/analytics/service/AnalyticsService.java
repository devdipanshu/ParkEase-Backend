package com.parkease.analytics.service;

import com.parkease.analytics.dto.*;
import com.parkease.analytics.entity.OccupancyLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {

    OccupancyLog logOccupancy(OccupancyLogRequest request);

    OccupancyResponse getOccupancyRate(Long lotId);

    OccupancyResponse getOccupancyRateBetween(Long lotId, LocalDateTime start, LocalDateTime end);

    List<PeakHourResponse> getPeakHours(Long lotId);

    List<PeakHourResponse> getPeakHoursBetween(Long lotId, LocalDateTime start, LocalDateTime end);

    List<VehicleTypeStatsResponse> getVehicleTypeStats(Long lotId);

    List<DailyTrendResponse> getDailyTrend(Long lotId, LocalDateTime start, LocalDateTime end);

    RevenueStatsResponse getRevenueByLot(Long lotId);

    RevenueStatsResponse getRevenueByDay(LocalDateTime start, LocalDateTime end);

    PlatformSummaryResponse getPlatformSummary();

    DailyReportResponse generateDailyReport(Long lotId, LocalDate date);
}
