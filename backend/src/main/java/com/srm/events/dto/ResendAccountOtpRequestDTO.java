package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResendAccountOtpRequestDTO(
        @NotBlank @Pattern(regexp = "STUDENT") String registryType,
        @NotBlank String identifier) { }
