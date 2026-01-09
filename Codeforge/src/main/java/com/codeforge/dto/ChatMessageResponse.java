package com.codeforge.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private String senderName;
    private String senderEmail;
    private String content;
    private LocalDateTime sentAt;

    public ChatMessageResponse(String senderName,
                               String senderEmail,
                               String content,
                               LocalDateTime sentAt) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.content = content;
        this.sentAt = sentAt;
    }

    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
}