package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AchievementVerificationRequestDTO {
    @NotBlank private String status;
    @Size(max = 1000) private String remarks;
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String value) { remarks = value; }
}
