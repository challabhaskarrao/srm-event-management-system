package com.srm.events.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FacultyProvisionRequestDTO(
        @NotNull @Positive Long registryId,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 200) String temporaryPassword,
        @NotBlank @Size(max = 200) String confirmTemporaryPassword) { }
