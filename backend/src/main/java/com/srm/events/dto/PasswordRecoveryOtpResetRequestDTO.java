package com.srm.events.dto;
import jakarta.validation.constraints.*;
public record PasswordRecoveryOtpResetRequestDTO(@NotBlank @Pattern(regexp="STUDENT|FACULTY") String registryType,@NotBlank String identifier,@NotBlank @Pattern(regexp="\\d{6}") String otp,@NotBlank String password,@NotBlank String confirmPassword) { }
