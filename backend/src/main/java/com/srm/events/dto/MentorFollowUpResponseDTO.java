package com.srm.events.dto;

import java.util.ArrayList;
import java.util.List;

public class MentorFollowUpResponseDTO {
    private MentorFollowUpSummaryDTO summary = new MentorFollowUpSummaryDTO();
    private List<MentorFollowUpItemDTO> items = new ArrayList<>();

    public MentorFollowUpSummaryDTO getSummary() { return summary; }
    public void setSummary(MentorFollowUpSummaryDTO value) { summary = value; }
    public List<MentorFollowUpItemDTO> getItems() { return items; }
    public void setItems(List<MentorFollowUpItemDTO> value) { items = value == null ? new ArrayList<>() : value; }
}
