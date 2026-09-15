package com.srm.events.dto;

import com.srm.events.model.Event;
import com.srm.events.model.EventMode;
import java.time.LocalDate;

public class EventResponseDTO {

    private Long eventId;
    private String eventName;
    private Long categoryId;
    private String categoryName;
    private String organizer;
    private LocalDate startDate;
    private LocalDate endDate;
    private String venue;
    private EventMode mode;
    private String eventUrl;
    private Long createdByUserId;
    private String createdByEmail;

    public EventResponseDTO() {
    }
    
    public EventResponseDTO(Event event) {
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.categoryId = event.getCategoryId();
        this.categoryName = event.getCategoryName();
        this.organizer = event.getOrganizer();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.venue = event.getVenue();
        this.mode = event.getMode();
        this.eventUrl = event.getEventUrl();
        this.createdByUserId = event.getCreatedByUserId();
        this.createdByEmail = event.getCreatedByEmail();
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
}
