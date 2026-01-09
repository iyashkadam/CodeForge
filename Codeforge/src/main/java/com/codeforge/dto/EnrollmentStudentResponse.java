package com.codeforge.dto;

public class EnrollmentStudentResponse {

    private Long studentId;
    private String name;
    private String email;
    private String branch;

    public EnrollmentStudentResponse(Long studentId, String name, String email, String branch) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.branch = branch;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBranch() {
        return branch;
    }
}