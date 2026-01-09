package com.codeforge.service;

import com.codeforge.dto.EnrollmentResponse;
import com.codeforge.dto.EnrollmentStudentResponse;
import com.codeforge.models.Course;
import com.codeforge.models.Enrollment;
import com.codeforge.models.User;
import com.codeforge.repository.CourseRepository;
import com.codeforge.repository.EnrollmentRepository;
import com.codeforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    // ✅ Enroll student
    public Enrollment enrollStudent(String email, Long courseId) {

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());

        return enrollmentRepository.save(enrollment);
    }

    // ✅ View my enrollments (FIXED)
    public List<EnrollmentResponse> getMyEnrollments(String email) {

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return enrollmentRepository.findByStudentWithCourse(student)
                .stream()
                .map(e -> new EnrollmentResponse(
                        e.getId(),
                        e.getCourse().getId(),
                        e.getCourse().getTitle(),
                        e.getCourse().getDescription(),
                        e.getEnrolledAt()
                ))
                .collect(Collectors.toList());
    }

    // ✅ Un-enroll student
    public void unEnrollStudent(String email, Long courseId) {

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = enrollmentRepository
                .findByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this course"));

        enrollmentRepository.delete(enrollment);
    }

    public List<EnrollmentStudentResponse> getStudentsByCourse(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return enrollmentRepository.findByCourse(course)
                .stream()
                .map(e -> new EnrollmentStudentResponse(
                        e.getStudent().getId(),
                        e.getStudent().getName(),
                        e.getStudent().getEmail(),
                        e.getStudent().getBranch()
                ))
                .toList();
    }
}