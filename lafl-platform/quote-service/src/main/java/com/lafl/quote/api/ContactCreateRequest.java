package com.lafl.quote.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactCreateRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    String company,
    @NotBlank String message
) {
}
