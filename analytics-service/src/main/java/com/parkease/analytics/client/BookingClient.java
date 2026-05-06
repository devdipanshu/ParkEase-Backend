package com.parkease.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/bookings/lot/{lotId}")
    List<Object> getBookingsByLot(@PathVariable("lotId") Long lotId);
}
