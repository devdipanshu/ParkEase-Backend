package com.parkease.analytics.repository;

import com.parkease.analytics.entity.OccupancyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<OccupancyLog, Long> {

    List<OccupancyLog> findByLotId(Long lotId);

    List<OccupancyLog> findByLotIdAndTimestampBetween(
            Long lotId, LocalDateTime start, LocalDateTime end);

    List<OccupancyLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    Long countByTimestampAfter(LocalDateTime since);

    @Query("SELECT COALESCE(AVG(o.occupancyRate), 0.0) FROM OccupancyLog o WHERE o.lotId = :lotId")
    Double avgOccupancyByLotId(@Param("lotId") Long lotId);

    @Query("""
            SELECT COALESCE(AVG(o.occupancyRate), 0.0)
            FROM OccupancyLog o
            WHERE o.lotId = :lotId
            AND o.timestamp BETWEEN :start AND :end
            """)
    Double avgOccupancyByLotIdBetween(
            @Param("lotId") Long lotId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT HOUR(timestamp) as hour,
                   COALESCE(AVG(occupancy_rate), 0) as avgOccupancy,
                   COUNT(*) as logCount
            FROM occupancy_logs
            WHERE lot_id = :lotId
            GROUP BY HOUR(timestamp)
            ORDER BY avgOccupancy DESC
            """, nativeQuery = true)
    List<Object[]> findPeakHoursByLotId(@Param("lotId") Long lotId);

    @Query(value = """
            SELECT HOUR(timestamp) as hour,
                   COALESCE(AVG(occupancy_rate), 0) as avgOccupancy,
                   COUNT(*) as logCount
            FROM occupancy_logs
            WHERE lot_id = :lotId
            AND timestamp BETWEEN :start AND :end
            GROUP BY HOUR(timestamp)
            ORDER BY avgOccupancy DESC
            """, nativeQuery = true)
    List<Object[]> findPeakHoursByLotIdBetween(
            @Param("lotId") Long lotId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT vehicle_type,
                   COUNT(*) as count,
                   COALESCE(AVG(occupancy_rate), 0) as avgOccupancy
            FROM occupancy_logs
            WHERE lot_id = :lotId
            AND vehicle_type IS NOT NULL
            GROUP BY vehicle_type
            ORDER BY count DESC
            """, nativeQuery = true)
    List<Object[]> findVehicleTypeDistribution(@Param("lotId") Long lotId);

    @Query(value = """
            SELECT DATE(timestamp) as date,
                   COALESCE(AVG(occupancy_rate), 0) as avgOccupancy,
                   MIN(available_spots) as minAvailable,
                   MAX(total_spots) as maxTotal
            FROM occupancy_logs
            WHERE lot_id = :lotId
            AND timestamp BETWEEN :start AND :end
            GROUP BY DATE(timestamp)
            ORDER BY date ASC
            """, nativeQuery = true)
    List<Object[]> findDailyTrendByLotId(
            @Param("lotId") Long lotId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
