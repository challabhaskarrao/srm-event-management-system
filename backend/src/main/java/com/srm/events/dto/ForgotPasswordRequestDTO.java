package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ForgotPasswordRequestDTO {
    @NotBlank(message = "Email is required")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
