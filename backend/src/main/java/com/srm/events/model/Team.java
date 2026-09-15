package com.srm.events.model;

import java.time.LocalDateTime;

public class Team {

    private Long teamId;
    private Long applicationId;
    private String teamName;
    private Long leaderStudentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Team() {
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getLeaderStudentId() {
        return leaderStudentId;
    }

    public void setLeaderStudentId(Long leaderStudentId) {
        this.leaderStudentId = leaderStudentId;
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
