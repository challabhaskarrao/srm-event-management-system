package com.srm.events.dto;

import com.srm.events.model.ParticipationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class RegisterApplicationRequestDTO {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Participation type is required")
    private ParticipationType participationType;

    private boolean travelRequired;
    private boolean outsideState;
    
    @Size(max = 150, message = "Team name must not exceed 150 characters")
    private String teamName;

    @Size(max = 20, message = "A team cannot contain more than 20 additional members")
    private List<String> teamMemberRegistrationNumbers = new ArrayList<>();

    public RegisterApplicationRequestDTO() {
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public ParticipationType getParticipationType() {
        return participationType;
    }

    public void setParticipationType(ParticipationType participationType) {
        this.participationType = participationType;
    }

    public boolean isTravelRequired() {
        return travelRequired;
    }

    public void setTravelRequired(boolean travelRequired) {
        this.travelRequired = travelRequired;
    }

    public boolean isOutsideState() {
        return outsideState;
    }

    public void setOutsideState(boolean outsideState) {
        this.outsideState = outsideState;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<String> getTeamMemberRegistrationNumbers() {
        return teamMemberRegistrationNumbers;
    }

    public void setTeamMemberRegistrationNumbers(List<String> teamMemberRegistrationNumbers) {
        this.teamMemberRegistrationNumbers = teamMemberRegistrationNumbers == null
                ? new ArrayList<>() : teamMemberRegistrationNumbers;
    }
}
