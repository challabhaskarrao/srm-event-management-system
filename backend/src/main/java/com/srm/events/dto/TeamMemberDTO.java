package com.srm.events.dto;

public class TeamMemberDTO {
    private Long studentId;
    private String registrationNumber;
    private String name;

    public TeamMemberDTO() {
    }

    public TeamMemberDTO(Long studentId, String registrationNumber, String name) {
        this.studentId = studentId;
        this.registrationNumber = registrationNumber;
        this.name = name;
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
