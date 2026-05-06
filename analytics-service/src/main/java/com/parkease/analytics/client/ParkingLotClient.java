package com.parkease.analytics.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "parkinglot-service")
public interface ParkingLotClient {

    @GetMapping("/lots")
    List<ParkingLotClient.LotSummary> getAllApprovedLots();

    @GetMapping("/lots/{id}")
    ParkingLotClient.LotSummary getLotById(@PathVariable("id") Long id);

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    class LotSummary {
        private Long lotId;
        private Integer totalSpots;
        private Integer availableSpots;
        private Boolean isOpen;
        private Boolean isApproved;
    }
}
