package com.srm.events.dto;

import com.srm.events.model.ParticipationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StudentAchievementRequestDTO {
    @NotBlank @Size(max = 80) private String eventTypeCode;
    @NotBlank @Size(max = 220) private String title;
    @NotBlank @Size(max = 220) private String organization;
    @Size(max = 4000) private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 500) private String officialUrl;
    private ParticipationType participationType = ParticipationType.INDIVIDUAL;
    @Size(max = 150) private String teamName;
    @Size(max = 9) private List<@Size(max = 30) String> teamMemberRegistrationNumbers;
    private Map<String, String> typeSpecificDetails;

    public String getEventTypeCode() { return eventTypeCode; }
    public void setEventTypeCode(String value) { eventTypeCode = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getOrganization() { return organization; }
    public void setOrganization(String value) { organization = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate value) { startDate = value; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate value) { endDate = value; }
    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String value) { officialUrl = value; }
    public ParticipationType getParticipationType() { return participationType; }
    public void setParticipationType(ParticipationType value) { participationType = value; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String value) { teamName = value; }
    public List<String> getTeamMemberRegistrationNumbers() { return teamMemberRegistrationNumbers; }
    public void setTeamMemberRegistrationNumbers(List<String> value) { teamMemberRegistrationNumbers = value; }
    public Map<String, String> getTypeSpecificDetails() { return typeSpecificDetails; }
    public void setTypeSpecificDetails(Map<String, String> value) { typeSpecificDetails = value; }
}
