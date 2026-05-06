package com.parkease.spot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkSpotRequest {

    @NotEmpty(message = "Spots list must not be empty")
    @Valid
    private List<SpotDTO> spots;
}
