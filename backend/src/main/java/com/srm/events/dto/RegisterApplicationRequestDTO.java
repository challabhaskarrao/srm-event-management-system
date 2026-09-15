package com.srm.events.dto;

import com.srm.events.model.ParticipationType;
import jakarta.validation.constraints.NotNull;

public class RegisterApplicationRequestDTO {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Participation type is required")
    private ParticipationType participationType;

    private boolean travelRequired;
    private boolean outsideState;
    
    private String teamName;

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
}
