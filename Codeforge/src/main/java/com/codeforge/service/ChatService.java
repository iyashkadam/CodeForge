package com.codeforge.service;

import com.codeforge.dto.ChatMessageResponse;
import com.codeforge.models.Course;
import com.codeforge.models.Message;
import com.codeforge.models.User;
import com.codeforge.repository.CourseRepository;
import com.codeforge.repository.EnrollmentRepository;
import com.codeforge.repository.MessageRepository;
import com.codeforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    // Send message
    public void sendMessage(String email, Long courseId, String content) {

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 🔐 Only enrolled students / admin can chat
        boolean isEnrolled = enrollmentRepository
                .existsByStudentAndCourse(sender, course);

        if (!isEnrolled && sender.getRole().name().equals("STUDENT")) {
            throw new RuntimeException("You are not enrolled in this course");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setCourse(course);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());

        messageRepository.save(message);
    }

    // Fetch messages
    public List<ChatMessageResponse> getMessages(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return messageRepository.findByCourseWithSender(course)
                .stream()
                .map(m -> new ChatMessageResponse(
                        m.getSender().getName(),
                        m.getSender().getEmail(),
                        m.getContent(),
                        m.getSentAt()
                ))
                .toList();
    }
}