package com.java.chat_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {
    @NotBlank
    @NotNull
    String conversationId;

    @NotBlank
    @NotNull
    String message;

    // Thông tin người gửi (truyền từ client)
    @NotBlank
    @NotNull
    String senderId;

    @NotBlank
    @NotNull
    String senderName;
}