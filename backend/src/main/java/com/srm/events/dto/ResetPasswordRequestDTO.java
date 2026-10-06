package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequestDTO {
    @NotBlank(message = "Reset token is required")
    @Size(max = 200, message = "Reset token is invalid")
    private String token;

    @NotBlank(message = "New password is required")
    private String newPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
