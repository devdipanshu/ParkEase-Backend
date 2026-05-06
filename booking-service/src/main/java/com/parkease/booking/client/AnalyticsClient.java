package com.parkease.booking.client;

import com.parkease.booking.dto.external.OccupancyLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "analytics-service")
public interface AnalyticsClient {

    @PostMapping("/analytics/occupancy")
    void logOccupancy(@RequestBody OccupancyLogRequest request);
}
