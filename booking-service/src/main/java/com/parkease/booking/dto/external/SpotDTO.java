package com.parkease.booking.dto.external;

import lombok.Data;

@Data
public class SpotDTO {

    private Long spotId;
    private Long lotId;
    private String status;
    private Double pricePerHour;
}
