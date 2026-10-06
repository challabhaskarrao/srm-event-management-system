package com.srm.events.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Event {

    private Long eventId;
    private String eventName;
    private Long categoryId;
    private String categoryName;
    private String eventTypeCode;
    private String eventTypeName;
    private String typeSpecificDetails;
    private String otherCategory;
    private String organizer;
    private LocalDate startDate;
    private LocalDate endDate;
    private String venue;
    private String city;
    private String state;
    private String country;
    private EventMode mode;
    private String eventUrl;
    private String description;
    private boolean registrationFeeRequired;
    private java.math.BigDecimal registrationFeeAmount;
    private Long createdByUserId;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Event() {
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public String getEventTypeCode() { return eventTypeCode; }
    public void setEventTypeCode(String value) { eventTypeCode = value; }
    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String value) { eventTypeName = value; }
    public String getTypeSpecificDetails() { return typeSpecificDetails; }
    public void setTypeSpecificDetails(String value) { typeSpecificDetails = value; }
    public String getOtherCategory() { return otherCategory; }
    public void setOtherCategory(String otherCategory) { this.otherCategory = otherCategory; }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public EventMode getMode() {
        return mode;
    }

    public void setMode(EventMode mode) {
        this.mode = mode;
    }

    public String getEventUrl() {
        return eventUrl;
    }

    public void setEventUrl(String eventUrl) {
        this.eventUrl = eventUrl;
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRegistrationFeeRequired() { return registrationFeeRequired; }
    public void setRegistrationFeeRequired(boolean registrationFeeRequired) { this.registrationFeeRequired = registrationFeeRequired; }
    public java.math.BigDecimal getRegistrationFeeAmount() { return registrationFeeAmount; }
    public void setRegistrationFeeAmount(java.math.BigDecimal registrationFeeAmount) { this.registrationFeeAmount = registrationFeeAmount; }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
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
}
