package com.codeforge.controller;

import com.codeforge.dto.ChatMessageResponse;
import com.codeforge.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Send message
    @PostMapping("/{courseId}")
    public ResponseEntity<String> sendMessage(
            @PathVariable Long courseId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        chatService.sendMessage(
                authentication.getName(),
                courseId,
                body.get("content")
        );

        return ResponseEntity.ok("Message sent");
    }

    // Get messages
    @GetMapping("/{courseId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(chatService.getMessages(courseId));
    }
}