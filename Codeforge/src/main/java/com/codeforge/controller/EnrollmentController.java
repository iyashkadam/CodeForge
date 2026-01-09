package com.codeforge.controller;

import com.codeforge.dto.EnrollmentResponse;
import com.codeforge.dto.EnrollmentStudentResponse;
import com.codeforge.models.Enrollment;
import com.codeforge.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<?> enroll(@PathVariable Long courseId,
                                    Authentication authentication) {

        enrollmentService.enrollStudent(authentication.getName(), courseId);
        return ResponseEntity.ok("Enrolled successfully");
    }

    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponse>> myEnrollments(
            Authentication authentication) {

        return ResponseEntity.ok(
                enrollmentService.getMyEnrollments(authentication.getName())
        );
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> unEnroll(
            @PathVariable Long courseId,
            Authentication authentication) {

        String email = authentication.getName();
        enrollmentService.unEnrollStudent(email, courseId);

        return ResponseEntity.ok("Un-enrolled successfully");
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EnrollmentStudentResponse> getStudentsByCourse(
            @PathVariable Long courseId) {

        return enrollmentService.getStudentsByCourse(courseId);
    }
}
