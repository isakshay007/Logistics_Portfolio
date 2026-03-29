package com.lafl.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String company,
    String phone,
    String interest,
    @NotBlank @Size(min = 8) String password
) {
}
