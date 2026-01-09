package com.codeforge.dto;

import java.time.LocalDateTime;

public class EnrollmentResponse {

    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private LocalDateTime enrolledAt;

    public EnrollmentResponse(Long enrollmentId, Long courseId,
                              String courseTitle, String courseDescription,
                              LocalDateTime enrolledAt) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseDescription = courseDescription;
        this.enrolledAt = enrolledAt;
    }

    public Long getEnrollmentId() { return enrollmentId; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getCourseDescription() { return courseDescription; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
}