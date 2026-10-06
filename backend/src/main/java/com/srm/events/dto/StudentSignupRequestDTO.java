package com.srm.events.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentSignupRequestDTO(
        @NotBlank @Size(max = 30) String registrationNumber,
        @NotBlank @Size(max = 30) String section,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 200) String password,
        @NotBlank @Size(max = 200) String confirmPassword) { }
