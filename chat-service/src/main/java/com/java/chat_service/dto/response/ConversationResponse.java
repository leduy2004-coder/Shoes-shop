package com.java.chat_service.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    String id;
    String participantsHash;
    String conversationName; // Tên của người còn lại trong conversation
    Instant createdDate;
    Instant modifiedDate;
}