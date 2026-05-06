package com.parkease.booking.client;

import com.parkease.booking.dto.external.LotDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "parkinglot-service")
public interface ParkingLotClient {

    @GetMapping("/lots/{id}")
    LotDTO getLot(@PathVariable("id") Long lotId);

    @PutMapping("/lots/{id}/decrement")
    void decrementAvailable(@PathVariable("id") Long lotId);

    @PutMapping("/lots/{id}/increment")
    void incrementAvailable(@PathVariable("id") Long lotId);
}
