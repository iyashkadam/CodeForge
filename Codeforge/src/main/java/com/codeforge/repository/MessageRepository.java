package com.codeforge.repository;

import com.codeforge.models.Course;
import com.codeforge.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.course = :course
        ORDER BY m.sentAt ASC
    """)
    List<Message> findByCourseWithSender(Course course);
}