package com.codeforge.repository;

import com.codeforge.models.Course;
import com.codeforge.models.Enrollment;
import com.codeforge.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // ✅ FETCH JOIN to avoid LazyInitializationException
    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.course
        WHERE e.student = :student
    """)
    List<Enrollment> findByStudentWithCourse(User student);

    boolean existsByStudentAndCourse(User student, com.codeforge.models.Course course);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);

    List<Enrollment> findByCourse(Course course);

    List<Enrollment> findByStudent(User student);

}