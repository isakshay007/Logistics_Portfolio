package com.lafl.quote.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record QuoteCreateRequest(
    @NotBlank String company,
    @NotBlank String contactName,
    @Email @NotBlank String email,
    @NotBlank String serviceType,
    @NotBlank String origin,
    @NotBlank String destination,
    String shipmentType,
    String cargoDetails
) {
}
