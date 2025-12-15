package com.java.chat_service.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import com.java.chat_service.dto.request.ChatMessageRequest;
import com.java.chat_service.dto.response.ChatMessageResponse;
import com.java.chat_service.entity.ChatMessage;
import com.java.chat_service.entity.Conversation;
import com.java.chat_service.entity.WebSocketSession;
import com.java.chat_service.exception.AppException;
import com.java.chat_service.exception.ErrorCode;
import com.java.chat_service.repository.ChatMessageRepository;
import com.java.chat_service.repository.ConversationRepository;
import com.java.chat_service.repository.WebSocketSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService {
    SocketIOServer socketIOServer;
    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    WebSocketSessionRepository webSocketSessionRepository;
    @Qualifier("redisObjectMapper")
    ObjectMapper objectMapper;

    public List<ChatMessageResponse> getMessages(String conversationId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Validate conversationId và kiểm tra user có trong conversation không
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isParticipant) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Validate: chỉ có 2 người trong conversation
        if (conversation.getParticipants().size() != 2) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        var messages = chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId);

        return messages.stream()
                .map(msg -> toChatMessageResponse(msg, userId))
                .toList();
    }

    public List<ChatMessageResponse> getMessagesByUserId(String userId) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Validate: chỉ cho phép lấy messages của chính mình (trừ khi là admin)
        boolean isAdmin = com.java.chat_service.utility.GetInfo.isAdmin();
        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Lấy tất cả conversations mà user tham gia
        List<Conversation> conversations = conversationRepository.findAllByParticipantIdsContains(userId);

        // Lấy danh sách conversationIds
        List<String> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .toList();

        if (conversationIds.isEmpty()) {
            return List.of();
        }

        // Lấy tất cả messages từ các conversations
        List<ChatMessage> messages = chatMessageRepository
                .findAllByConversationIdInOrderByCreatedDateDesc(conversationIds);

        // Convert sang response và sắp xếp từ mới đến cũ
        return messages.stream()
                .map(msg -> toChatMessageResponse(msg, userId))
                .sorted((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))
                .toList();
    }

    public ChatMessageResponse create(ChatMessageRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Validate: senderId phải khớp với user hiện tại
        if (!userId.equals(request.getSenderId())) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Validate conversationId và kiểm tra user có trong conversation không
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isParticipant) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Validate: chỉ có 2 người trong conversation
        if (conversation.getParticipants().size() != 2) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Tạo message với thông tin từ request
        ChatMessage chatMessage = ChatMessage.builder()
                .conversationId(request.getConversationId())
                .message(request.getMessage())
                .senderId(request.getSenderId())
                .senderName(request.getSenderName())
                .createdDate(Instant.now())
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);

        // Cập nhật modifiedDate của conversation
        conversation.setModifiedDate(Instant.now());
        conversationRepository.save(conversation);

        // Gửi message qua WebSocket đến người còn lại
        sendMessageViaWebSocket(chatMessage, conversation, userId);

        return toChatMessageResponse(chatMessage, userId);
    }

    private void sendMessageViaWebSocket(ChatMessage chatMessage, Conversation conversation, String senderId) {
        try {
            // Lấy danh sách userIds của 2 người trong conversation
            List<String> userIds = conversation.getParticipants().stream()
                    .map(Conversation.ParticipantInfo::getUserId)
                    .toList();

            // Lấy các WebSocket sessions
            Map<String, WebSocketSession> webSocketSessions = webSocketSessionRepository
                    .findAllByUserIdIn(userIds).stream()
                    .collect(Collectors.toMap(
                            WebSocketSession::getSocketSessionId,
                            Function.identity()));

            // Tạo response
            ChatMessageResponse response = toChatMessageResponse(chatMessage, senderId);

            // Gửi đến tất cả clients trong conversation
            socketIOServer.getAllClients().forEach(client -> {
                WebSocketSession session = webSocketSessions.get(client.getSessionId().toString());

                if (Objects.nonNull(session)) {
                    try {
                        // Set flag "me" cho từng client
                        response.setMe(session.getUserId().equals(senderId));
                        String messageJson = objectMapper.writeValueAsString(response);
                        client.sendEvent("message", messageJson);
                        log.info("Message sent via WebSocket to user: {}", session.getUserId());
                    } catch (Exception e) {
                        log.error("Error sending message via WebSocket: {}", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error in sendMessageViaWebSocket: {}", e.getMessage(), e);
        }
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage, String currentUserId) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .conversationId(chatMessage.getConversationId())
                .message(chatMessage.getMessage())
                .senderId(chatMessage.getSenderId())
                .senderName(chatMessage.getSenderName())
                .me(currentUserId.equals(chatMessage.getSenderId()))
                .createdDate(chatMessage.getCreatedDate())
                .build();
    }
}