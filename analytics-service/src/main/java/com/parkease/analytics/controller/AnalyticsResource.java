package com.parkease.analytics.controller;

import com.parkease.analytics.dto.*;
import com.parkease.analytics.entity.OccupancyLog;
import com.parkease.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @PostMapping("/occupancy")
    public ResponseEntity<OccupancyLog> logOccupancy(@Valid @RequestBody OccupancyLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analyticsService.logOccupancy(request));
    }

    @GetMapping("/occupancy/{lotId}")
    public ResponseEntity<OccupancyResponse> getOccupancy(@PathVariable Long lotId) {
        return ResponseEntity.ok(analyticsService.getOccupancyRate(lotId));
    }

    @GetMapping("/occupancy/{lotId}/range")
    public ResponseEntity<OccupancyResponse> getOccupancyInRange(
            @PathVariable Long lotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(analyticsService.getOccupancyRateBetween(lotId, start, end));
    }

    @GetMapping("/peak-hours/{lotId}")
    public ResponseEntity<List<PeakHourResponse>> getPeakHours(@PathVariable Long lotId) {
        return ResponseEntity.ok(analyticsService.getPeakHours(lotId));
    }

    @GetMapping("/peak-hours/{lotId}/range")
    public ResponseEntity<List<PeakHourResponse>> getPeakHoursInRange(
            @PathVariable Long lotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(analyticsService.getPeakHoursBetween(lotId, start, end));
    }

    @GetMapping("/vehicle-stats/{lotId}")
    public ResponseEntity<List<VehicleTypeStatsResponse>> getVehicleStats(@PathVariable Long lotId) {
        return ResponseEntity.ok(analyticsService.getVehicleTypeStats(lotId));
    }

    @GetMapping("/trend/{lotId}")
    public ResponseEntity<List<DailyTrendResponse>> getDailyTrend(
            @PathVariable Long lotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(analyticsService.getDailyTrend(lotId, start, end));
    }

    @GetMapping("/revenue/{lotId}")
    public ResponseEntity<RevenueStatsResponse> getRevenueByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(analyticsService.getRevenueByLot(lotId));
    }

    @GetMapping("/revenue/range")
    public ResponseEntity<RevenueStatsResponse> getRevenueInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(analyticsService.getRevenueByDay(start, end));
    }

    @GetMapping("/summary")
    public ResponseEntity<PlatformSummaryResponse> getPlatformSummary() {
        return ResponseEntity.ok(analyticsService.getPlatformSummary());
    }

    @GetMapping("/report/{lotId}")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @PathVariable Long lotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.generateDailyReport(lotId, date));
    }
}
