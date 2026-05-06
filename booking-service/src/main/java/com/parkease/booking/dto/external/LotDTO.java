package com.parkease.booking.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LotDTO {
    private Long lotId;
    private Integer totalSpots;
    private Integer availableSpots;
}
