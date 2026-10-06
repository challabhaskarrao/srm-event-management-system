package com.srm.events.dto;

public class StudentProfileDTO {
    private Long studentId;
    private String registrationNumber;
    private String name;
    private String email;
    private String departmentCode;
    private String departmentName;
    private String sectionName;
    private String academicYear;
    private int studentYear;
    private int semester;
    private String batch;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public int getStudentYear() { return studentYear; }
    public void setStudentYear(int studentYear) { this.studentYear = studentYear; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
}
