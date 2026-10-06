package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewApplicationRequestDTO {
    
    @NotBlank(message = "Status cannot be blank")
    private String status;
    
    private String remarks;

    public ReviewApplicationRequestDTO() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
