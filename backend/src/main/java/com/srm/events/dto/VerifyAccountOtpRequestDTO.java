package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyAccountOtpRequestDTO(
        @NotBlank @Pattern(regexp = "STUDENT") String registryType,
        @NotBlank @Size(max = 30) String identifier,
        @NotBlank @Pattern(regexp = "\\d{6}") String otp) { }
