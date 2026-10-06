package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostEventReportRequestDTO {
    @NotBlank
    private String participationStatus;
    @Size(max = 1000)
    private String noParticipationReason;
    @Size(max = 30)
    private String resultType;
    @Size(max = 100)
    private String position;
    @Size(max = 500)
    private String resultTitle;
    @Size(max = 950)
    private String remarks;
    @Size(max = 950)
    private String reflection;
    @Size(max = 500)
    private String resultUrl;

    public String getParticipationStatus() { return participationStatus; }
    public void setParticipationStatus(String value) { participationStatus = value; }
    public String getNoParticipationReason() { return noParticipationReason; }
    public void setNoParticipationReason(String value) { noParticipationReason = value; }
    public String getResultType() { return resultType; }
    public void setResultType(String value) { resultType = value; }
    public String getPosition() { return position; }
    public void setPosition(String value) { position = value; }
    public String getResultTitle() { return resultTitle; }
    public void setResultTitle(String value) { resultTitle = value; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String value) { remarks = value; }
    public String getReflection() { return reflection; }
    public void setReflection(String value) { reflection = value; }
    public String getResultUrl() { return resultUrl; }
    public void setResultUrl(String value) { resultUrl = value; }
}
