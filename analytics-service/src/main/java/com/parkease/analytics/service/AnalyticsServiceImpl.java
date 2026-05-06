package com.parkease.analytics.service;

import com.parkease.analytics.client.BookingClient;
import com.parkease.analytics.client.ParkingLotClient;
import com.parkease.analytics.client.PaymentClient;
import com.parkease.analytics.dto.*;
import com.parkease.analytics.entity.OccupancyLog;
import com.parkease.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final PaymentClient paymentClient;
    private final BookingClient bookingClient;
    private final ParkingLotClient parkingLotClient;

    @Override
    @Transactional
    public OccupancyLog logOccupancy(OccupancyLogRequest request) {
        OccupancyLog occupancyLog = OccupancyLog.builder()
                .lotId(request.getLotId())
                .spotId(request.getSpotId())
                .occupancyRate(request.getOccupancyRate())
                .availableSpots(request.getAvailableSpots())
                .totalSpots(request.getTotalSpots())
                .vehicleType(request.getVehicleType())
                .timestamp(request.getTimestamp())
                .build();
        OccupancyLog saved = analyticsRepository.save(occupancyLog);
        log.info("Logged occupancy for lot {}: {}%", request.getLotId(), request.getOccupancyRate());
        return saved;
    }

    @Override
    public OccupancyResponse getOccupancyRate(Long lotId) {
        List<OccupancyLog> logs = analyticsRepository.findByLotId(lotId);
        Double avg = analyticsRepository.avgOccupancyByLotId(lotId);

        // Derive current occupancy from the live lot data (always reflects reality)
        double currentOccupancy = 0.0;
        int availableSpots = 0;
        int totalSpots = 0;
        try {
            ParkingLotClient.LotSummary lot = parkingLotClient.getLotById(lotId);
            totalSpots = lot.getTotalSpots() != null ? lot.getTotalSpots() : 0;
            availableSpots = lot.getAvailableSpots() != null ? lot.getAvailableSpots() : 0;
            if (totalSpots > 0) {
                currentOccupancy = ((double)(totalSpots - availableSpots) / totalSpots) * 100.0;
            }
        } catch (Exception e) {
            // Fall back to last log entry if lot service unavailable
            if (!logs.isEmpty()) {
                OccupancyLog last = logs.get(logs.size() - 1);
                currentOccupancy = last.getOccupancyRate();
                availableSpots = last.getAvailableSpots() != null ? last.getAvailableSpots() : 0;
                totalSpots = last.getTotalSpots() != null ? last.getTotalSpots() : 0;
            }
            log.warn("Could not fetch live lot data for occupancy of lot {}: {}", lotId, e.getMessage());
        }

        return OccupancyResponse.builder()
                .lotId(lotId)
                .averageOccupancy(avg != null ? avg : currentOccupancy)
                .currentOccupancy(currentOccupancy)
                .totalLogs(logs.size())
                .build();
    }

    @Override
    public OccupancyResponse getOccupancyRateBetween(Long lotId, LocalDateTime start, LocalDateTime end) {
        Double avg = analyticsRepository.avgOccupancyByLotIdBetween(lotId, start, end);
        List<OccupancyLog> logs = analyticsRepository.findByLotIdAndTimestampBetween(lotId, start, end);
        Double current = logs.isEmpty() ? 0.0 : logs.get(logs.size() - 1).getOccupancyRate();
        return OccupancyResponse.builder()
                .lotId(lotId)
                .averageOccupancy(avg != null ? avg : 0.0)
                .currentOccupancy(current)
                .totalLogs(logs.size())
                .periodStart(start)
                .periodEnd(end)
                .build();
    }

    @Override
    public List<PeakHourResponse> getPeakHours(Long lotId) {
        return mapToPeakHourResponse(analyticsRepository.findPeakHoursByLotId(lotId));
    }

    @Override
    public List<PeakHourResponse> getPeakHoursBetween(Long lotId, LocalDateTime start, LocalDateTime end) {
        return mapToPeakHourResponse(analyticsRepository.findPeakHoursByLotIdBetween(lotId, start, end));
    }

    @Override
    public List<VehicleTypeStatsResponse> getVehicleTypeStats(Long lotId) {
        List<Object[]> rows = analyticsRepository.findVehicleTypeDistribution(lotId);
        if (rows.isEmpty()) return Collections.emptyList();

        long totalCount = rows.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return rows.stream()
                .map(row -> VehicleTypeStatsResponse.builder()
                        .vehicleType((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .avgOccupancy(((Number) row[2]).doubleValue())
                        .percentage(totalCount > 0
                                ? (((Number) row[1]).doubleValue() * 100.0) / totalCount
                                : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DailyTrendResponse> getDailyTrend(Long lotId, LocalDateTime start, LocalDateTime end) {
        return analyticsRepository.findDailyTrendByLotId(lotId, start, end).stream()
                .map(row -> DailyTrendResponse.builder()
                        .date(row[0].toString())
                        .avgOccupancy(((Number) row[1]).doubleValue())
                        .minAvailableSpots(row[2] != null ? ((Number) row[2]).intValue() : null)
                        .maxTotalSpots(row[3] != null ? ((Number) row[3]).intValue() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public RevenueStatsResponse getRevenueByLot(Long lotId) {
        Double revenue = 0.0;
        try {
            revenue = paymentClient.getRevenueByLot(lotId);
        } catch (Exception e) {
            log.warn("Payment service unavailable for lot {}: {}", lotId, e.getMessage());
        }
        int logCount = analyticsRepository.findByLotId(lotId).size();
        double avg = logCount > 0 && revenue > 0 ? revenue / logCount : 0.0;
        return RevenueStatsResponse.builder()
                .lotId(lotId)
                .totalRevenue(revenue)
                .transactionCount(logCount)
                .averageTransactionAmount(avg)
                .build();
    }

    @Override
    public RevenueStatsResponse getRevenueByDay(LocalDateTime start, LocalDateTime end) {
        Double revenue = 0.0;
        try {
            revenue = paymentClient.getRevenueBetween(start, end);
        } catch (Exception e) {
            log.warn("Payment service unavailable for range query: {}", e.getMessage());
        }
        int logCount = analyticsRepository.findByTimestampBetween(start, end).size();
        double avg = logCount > 0 && revenue > 0 ? revenue / logCount : 0.0;
        return RevenueStatsResponse.builder()
                .totalRevenue(revenue)
                .transactionCount(logCount)
                .averageTransactionAmount(avg)
                .periodStart(start)
                .periodEnd(end)
                .build();
    }

    @Override
    public PlatformSummaryResponse getPlatformSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Revenue from payment service
        Double revenueToday = 0.0;
        try { revenueToday = paymentClient.getRevenueBetween(todayStart, now); }
        catch (Exception e) { log.warn("Payment service unavailable for today revenue: {}", e.getMessage()); }

        Double revenueMonth = 0.0;
        try { revenueMonth = paymentClient.getRevenueBetween(monthStart, now); }
        catch (Exception e) { log.warn("Payment service unavailable for month revenue: {}", e.getMessage()); }

        // Bookings count from occupancy logs (logged on each booking event)
        Long bookingsToday = analyticsRepository.countByTimestampAfter(todayStart);
        Long bookingsMonth = analyticsRepository.countByTimestampAfter(monthStart);

        // Real lot stats from parking lot service
        int totalActiveLots = 0;
        int totalSpots = 0;
        int totalAvailableSpots = 0;
        double platformOccupancy = 0.0;

        try {
            List<ParkingLotClient.LotSummary> lots = parkingLotClient.getAllApprovedLots();
            totalActiveLots = (int) lots.stream().filter(l -> Boolean.TRUE.equals(l.getIsOpen())).count();
            totalSpots = lots.stream().mapToInt(l -> l.getTotalSpots() != null ? l.getTotalSpots() : 0).sum();
            totalAvailableSpots = lots.stream().mapToInt(l -> l.getAvailableSpots() != null ? l.getAvailableSpots() : 0).sum();
            if (totalSpots > 0) {
                platformOccupancy = ((double)(totalSpots - totalAvailableSpots) / totalSpots) * 100.0;
            }
        } catch (Exception e) {
            log.warn("Parking lot service unavailable for platform summary: {}", e.getMessage());
            // Fall back to occupancy log-based calculation
            List<OccupancyLog> todayLogs = analyticsRepository.findByTimestampBetween(todayStart, now);
            platformOccupancy = todayLogs.isEmpty() ? 0.0
                    : todayLogs.stream().mapToDouble(OccupancyLog::getOccupancyRate).average().orElse(0.0);
        }

        // Peak hour from today's occupancy logs
        String peakHour = "N/A";
        List<OccupancyLog> todayLogs = analyticsRepository.findByTimestampBetween(todayStart, now);
        if (!todayLogs.isEmpty()) {
            int peak = todayLogs.stream()
                    .collect(Collectors.groupingBy(
                            l -> l.getTimestamp().getHour(),
                            Collectors.averagingDouble(OccupancyLog::getOccupancyRate)))
                    .entrySet().stream()
                    .max(java.util.Map.Entry.comparingByValue())
                    .map(java.util.Map.Entry::getKey)
                    .orElse(-1);
            if (peak >= 0) peakHour = formatHour(peak);
        }

        return PlatformSummaryResponse.builder()
                .totalActiveLots(totalActiveLots)
                .totalSpots(totalSpots)
                .totalAvailableSpots(totalAvailableSpots)
                .platformOccupancyRate(platformOccupancy)
                .totalRevenueToday(revenueToday != null ? revenueToday : 0.0)
                .totalRevenueThisMonth(revenueMonth != null ? revenueMonth : 0.0)
                .totalBookingsToday(bookingsToday.intValue())
                .totalBookingsThisMonth(bookingsMonth.intValue())
                .peakHourToday(peakHour)
                .generatedAt(now)
                .build();
    }

    @Override
    public DailyReportResponse generateDailyReport(Long lotId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        Double avgOccupancy = analyticsRepository.avgOccupancyByLotIdBetween(lotId, start, end);
        List<PeakHourResponse> hourly = getPeakHoursBetween(lotId, start, end);
        List<VehicleTypeStatsResponse> vehicleStats = getVehicleTypeStats(lotId);
        Double revenue = getRevenueByLot(lotId).getTotalRevenue();

        Integer peakHour = hourly.isEmpty() ? null : hourly.get(0).getHour();
        Double peakOccupancy = hourly.stream()
                .mapToDouble(PeakHourResponse::getAvgOccupancy)
                .max()
                .orElse(0.0);

        int totalBookings = analyticsRepository.findByLotIdAndTimestampBetween(lotId, start, end).size();

        log.info("Daily report generated for lot {} on {}", lotId, date);

        return DailyReportResponse.builder()
                .reportDate(date.toString())
                .lotId(lotId)
                .lotName("Lot #" + lotId)
                .avgOccupancy(avgOccupancy != null ? avgOccupancy : 0.0)
                .peakOccupancy(peakOccupancy)
                .peakHour(peakHour)
                .totalRevenue(revenue)
                .totalBookings(totalBookings)
                .hourlyBreakdown(hourly)
                .vehicleTypeBreakdown(vehicleStats)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<PeakHourResponse> mapToPeakHourResponse(List<Object[]> rows) {
        return rows.stream()
                .map(row -> {
                    int hour = ((Number) row[0]).intValue();
                    double avg = ((Number) row[1]).doubleValue();
                    long count = ((Number) row[2]).longValue();
                    return PeakHourResponse.builder()
                            .hour(hour)
                            .hourLabel(String.format("%02d:00 - %02d:00", hour, hour + 1))
                            .avgOccupancy(avg)
                            .logCount(count)
                            .isPeak(avg > 80.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String formatHour(int hour) {
        String amPm = hour < 12 ? "AM" : "PM";
        int display = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String nextAmPm = (hour + 1) < 12 ? "AM" : "PM";
        int nextDisplay = (hour + 1) == 0 ? 12 : ((hour + 1) > 12 ? (hour + 1) - 12 : (hour + 1));
        return display + " " + amPm + " - " + nextDisplay + " " + nextAmPm;
    }
}
