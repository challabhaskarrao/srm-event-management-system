package com.srm.events.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record PasswordRecoveryOtpStartRequestDTO(@NotBlank @Pattern(regexp="STUDENT|FACULTY") String registryType,@NotBlank String identifier) { }
