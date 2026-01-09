package com.codeforge.service;

import com.codeforge.models.Course;
import com.codeforge.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // ADMIN → create course
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    // STUDENT + ADMIN → list courses
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
}