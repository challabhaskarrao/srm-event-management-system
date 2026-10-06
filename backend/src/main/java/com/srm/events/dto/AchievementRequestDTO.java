package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AchievementRequestDTO {
    @NotBlank
    private String resultType;
    @Size(max = 100)
    private String position;
    @Size(max = 500)
    private String prizeDetails;
    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPrizeDetails() { return prizeDetails; }
    public void setPrizeDetails(String prizeDetails) { this.prizeDetails = prizeDetails; }
}
