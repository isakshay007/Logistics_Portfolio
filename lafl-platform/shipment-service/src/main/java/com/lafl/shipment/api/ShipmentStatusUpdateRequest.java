package com.lafl.shipment.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ShipmentStatusUpdateRequest(
    @NotBlank String status,
    @NotBlank String currentLocation,
    @Min(0) @Max(100) int progress,
    String note
) {
}
