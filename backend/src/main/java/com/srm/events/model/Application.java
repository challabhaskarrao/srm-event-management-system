package com.srm.events.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class Application {

    private Long applicationId;
    private String applicationCode;
    private Long eventId;
    private Long createdByStudentId;
    private Long assignedMentorId;
    private Long academicYearId;
    private Integer applicationSemester;
    private Integer applicationStudentYear;
    private ParticipationType participationType;
    private boolean travelRequired;
    private boolean outsideState;
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
    private boolean registrationFeePaid;
    private BigDecimal registrationFeeAmount;
    private ApplicationStatus currentStatus;
    private String correctionReturnStage;
    private LocalDateTime submittedAt;
    private LocalDateTime mentorReviewedAt;
    private LocalDateTime hodReviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long hodId;
    private String mentorRemarks;
    private String hodRemarks;
    private String participationStatus;
    private LocalDateTime postEventReportedAt;
    private String outcomeNotes;
    private String resultUrl;

    public Application() {
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

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

    public Long getCreatedByStudentId() {
        return createdByStudentId;
    }

    public void setCreatedByStudentId(Long createdByStudentId) {
        this.createdByStudentId = createdByStudentId;
    }

    public Long getAssignedMentorId() {
        return assignedMentorId;
    }

    public void setAssignedMentorId(Long assignedMentorId) {
        this.assignedMentorId = assignedMentorId;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Integer getApplicationSemester() { return applicationSemester; }
    public void setApplicationSemester(Integer value) { applicationSemester = value; }
    public Integer getApplicationStudentYear() { return applicationStudentYear; }
    public void setApplicationStudentYear(Integer value) { applicationStudentYear = value; }

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
    public boolean isRegistrationFeePaid() { return registrationFeePaid; }
    public void setRegistrationFeePaid(boolean value) { registrationFeePaid = value; }
    public BigDecimal getRegistrationFeeAmount() { return registrationFeeAmount; }
    public void setRegistrationFeeAmount(BigDecimal value) { registrationFeeAmount = value; }

    public ApplicationStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ApplicationStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getCorrectionReturnStage() {
        return correctionReturnStage;
    }

    public void setCorrectionReturnStage(String correctionReturnStage) {
        this.correctionReturnStage = correctionReturnStage;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getMentorReviewedAt() {
        return mentorReviewedAt;
    }

    public void setMentorReviewedAt(LocalDateTime mentorReviewedAt) {
        this.mentorReviewedAt = mentorReviewedAt;
    }

    public LocalDateTime getHodReviewedAt() {
        return hodReviewedAt;
    }

    public void setHodReviewedAt(LocalDateTime hodReviewedAt) {
        this.hodReviewedAt = hodReviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getHodId() {
        return hodId;
    }

    public void setHodId(Long hodId) {
        this.hodId = hodId;
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

    public String getParticipationStatus() { return participationStatus; }
    public void setParticipationStatus(String participationStatus) { this.participationStatus = participationStatus; }
    public LocalDateTime getPostEventReportedAt() { return postEventReportedAt; }
    public void setPostEventReportedAt(LocalDateTime postEventReportedAt) { this.postEventReportedAt = postEventReportedAt; }
    public String getOutcomeNotes() { return outcomeNotes; }
    public void setOutcomeNotes(String outcomeNotes) { this.outcomeNotes = outcomeNotes; }
    public String getResultUrl() { return resultUrl; }
    public void setResultUrl(String resultUrl) { this.resultUrl = resultUrl; }
}
