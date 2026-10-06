package com.srm.events.dto;

import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.ParticipationType;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ApplicationResponseDTO {

    private Long applicationId;
    private String applicationCode;
    private Long eventId;
    private String eventName;
    private String organizer;
    private String venue;
    private String eventMode;
    private String eventUrl;
    private String otherCategory;
    private String city;
    private String state;
    private String country;
    private String description;
    private boolean registrationFeeRequired;
    private BigDecimal registrationFeeAmount;
    private Long studentId;
    private String studentName;
    private String registrationNumber;
    private String eventType;
    private String eventTypeCode;
    private String typeSpecificDetails;
    private LocalDate startDate;
    private LocalDate endDate;
    private ParticipationType participationType;
    private boolean outsideState;
    private boolean travelRequired;
    private String destinationCity;
    private String destinationState;
    private String destinationCountry;
    private String venueAddress;
    private String modeOfTravel;
    private LocalDateTime departureAt;
    private LocalDateTime returnAt;
    private boolean accommodationRequired;
    private String accommodationName;
    private String accommodationAddress;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String guardianContactNumber;
    private String travelRemarks;
    private String teamName;
    private List<TeamMemberDTO> teamMembers = new ArrayList<>();
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private String mentorRemarks;
    private String hodRemarks;
    private Long assignedMentorId;
    private Long hodId;
    private LocalDateTime mentorReviewedAt;
    private LocalDateTime hodReviewedAt;
    private List<ApprovalActionDTO> approvalHistory = new ArrayList<>();
    private String participationStatus;
    private LocalDateTime postEventReportedAt;
    private String outcomeNotes;
    private String resultUrl;
    private String postEventStatus;
    private Long postEventDaysPending;
    private AchievementResponseDTO achievement;

    public ApplicationResponseDTO() {
    }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getEventMode() { return eventMode; }
    public void setEventMode(String eventMode) { this.eventMode = eventMode; }
    public String getEventUrl() { return eventUrl; }
    public void setEventUrl(String eventUrl) { this.eventUrl = eventUrl; }
    public String getOtherCategory() { return otherCategory; }
    public void setOtherCategory(String value) { otherCategory = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { country = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public boolean isRegistrationFeeRequired() { return registrationFeeRequired; }
    public void setRegistrationFeeRequired(boolean value) { registrationFeeRequired = value; }
    public BigDecimal getRegistrationFeeAmount() { return registrationFeeAmount; }
    public void setRegistrationFeeAmount(BigDecimal value) { registrationFeeAmount = value; }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventTypeCode() { return eventTypeCode; }
    public void setEventTypeCode(String value) { eventTypeCode = value; }
    public String getTypeSpecificDetails() { return typeSpecificDetails; }
    public void setTypeSpecificDetails(String value) { typeSpecificDetails = value; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ParticipationType getParticipationType() {
        return participationType;
    }

    public void setParticipationType(ParticipationType participationType) {
        this.participationType = participationType;
    }

    public boolean isOutsideState() { return outsideState; }
    public void setOutsideState(boolean outsideState) { this.outsideState = outsideState; }
    public boolean isTravelRequired() { return travelRequired; }
    public void setTravelRequired(boolean travelRequired) { this.travelRequired = travelRequired; }
    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String value) { destinationCity = value; }
    public String getDestinationState() { return destinationState; }
    public void setDestinationState(String value) { destinationState = value; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String value) { destinationCountry = value; }
    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String value) { venueAddress = value; }
    public String getModeOfTravel() { return modeOfTravel; }
    public void setModeOfTravel(String value) { modeOfTravel = value; }
    public LocalDateTime getDepartureAt() { return departureAt; }
    public void setDepartureAt(LocalDateTime value) { departureAt = value; }
    public LocalDateTime getReturnAt() { return returnAt; }
    public void setReturnAt(LocalDateTime value) { returnAt = value; }
    public boolean isAccommodationRequired() { return accommodationRequired; }
    public void setAccommodationRequired(boolean value) { accommodationRequired = value; }
    public String getAccommodationName() { return accommodationName; }
    public void setAccommodationName(String value) { accommodationName = value; }
    public String getAccommodationAddress() { return accommodationAddress; }
    public void setAccommodationAddress(String value) { accommodationAddress = value; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate value) { checkInDate = value; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate value) { checkOutDate = value; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String value) { emergencyContactName = value; }
    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String value) { emergencyContactNumber = value; }
    public String getGuardianContactNumber() { return guardianContactNumber; }
    public void setGuardianContactNumber(String value) { guardianContactNumber = value; }
    public String getTravelRemarks() { return travelRemarks; }
    public void setTravelRemarks(String value) { travelRemarks = value; }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<TeamMemberDTO> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(List<TeamMemberDTO> teamMembers) {
        this.teamMembers = teamMembers == null ? new ArrayList<>() : teamMembers;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getMentorRemarks() {
        return mentorRemarks;
    }

    public void setMentorRemarks(String mentorRemarks) {
        this.mentorRemarks = mentorRemarks;
    }

    public String getHodRemarks() {
        return hodRemarks;
    }

    public void setHodRemarks(String hodRemarks) {
        this.hodRemarks = hodRemarks;
    }

    public Long getAssignedMentorId() { return assignedMentorId; }
    public void setAssignedMentorId(Long assignedMentorId) { this.assignedMentorId = assignedMentorId; }
    public Long getHodId() { return hodId; }
    public void setHodId(Long hodId) { this.hodId = hodId; }
    public LocalDateTime getMentorReviewedAt() { return mentorReviewedAt; }
    public void setMentorReviewedAt(LocalDateTime mentorReviewedAt) { this.mentorReviewedAt = mentorReviewedAt; }
    public LocalDateTime getHodReviewedAt() { return hodReviewedAt; }
    public void setHodReviewedAt(LocalDateTime hodReviewedAt) { this.hodReviewedAt = hodReviewedAt; }

    public List<ApprovalActionDTO> getApprovalHistory() { return approvalHistory; }
    public void setApprovalHistory(List<ApprovalActionDTO> approvalHistory) {
        this.approvalHistory = approvalHistory == null ? new ArrayList<>() : approvalHistory;
    }

    public String getParticipationStatus() { return participationStatus; }
    public void setParticipationStatus(String value) { participationStatus = value; }
    public LocalDateTime getPostEventReportedAt() { return postEventReportedAt; }
    public void setPostEventReportedAt(LocalDateTime value) { postEventReportedAt = value; }
    public String getOutcomeNotes() { return outcomeNotes; }
    public void setOutcomeNotes(String value) { outcomeNotes = value; }
    public String getResultUrl() { return resultUrl; }
    public void setResultUrl(String value) { resultUrl = value; }
    public String getPostEventStatus() { return postEventStatus; }
    public void setPostEventStatus(String value) { postEventStatus = value; }
    public Long getPostEventDaysPending() { return postEventDaysPending; }
    public void setPostEventDaysPending(Long value) { postEventDaysPending = value; }
    public AchievementResponseDTO getAchievement() { return achievement; }
    public void setAchievement(AchievementResponseDTO value) { achievement = value; }
}
