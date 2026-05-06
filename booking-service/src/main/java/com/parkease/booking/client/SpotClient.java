package com.parkease.booking.client;

import com.parkease.booking.dto.external.SpotDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "spot-service")
public interface SpotClient {

    @GetMapping("/spots/{id}")
    SpotDTO getSpot(@PathVariable("id") Long spotId);

    @PutMapping("/spots/{id}/occupy")
    void reserveSpot(@PathVariable("id") Long spotId);

    @PutMapping("/spots/{id}/checkin")
    void checkInSpot(@PathVariable("id") Long spotId);

    @PutMapping("/spots/{id}/release")
    void releaseSpot(@PathVariable("id") Long spotId);
}
