package com.java.chat_service.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.java.chat_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
    ObjectMapper objectMapper;

    public List<ChatMessageResponse> getMessages(String conversationId) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        boolean isAdmin = GetInfo.isAdmin();

        // Validate quyền truy cập
        if (isAdmin) {
            // Admin chỉ xem được conversations của mình
            if (!conversation.getAdminId().equals(currentUserId)) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        } else {
            // User chỉ xem được conversation của mình
            if (!conversation.getUserId().equals(currentUserId)) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        // Lấy messages
        List<ChatMessage> messages = chatMessageRepository
                .findAllByConversationIdOrderByCreatedDateDesc(conversationId);

        return messages.stream()
                .map(msg -> toChatMessageResponse(msg, currentUserId))
                .toList();
    }

    public List<ChatMessageResponse> getMessagesByUserId(String userId) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = GetInfo.isAdmin();

        // Validate: chỉ cho phép lấy messages của chính mình (trừ khi là admin)
        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        List<Conversation> conversations;

        if (isAdmin) {
            // Admin lấy tất cả conversations
            conversations = conversationRepository.findAllByAdminIdOrderByModifiedDateDesc(userId);
        } else {
            // User chỉ có 1 conversation
            Conversation conversation = conversationRepository.findByUserId(userId)
                    .orElse(null);
            conversations = conversation != null ? List.of(conversation) : List.of();
        }

        if (conversations.isEmpty()) {
            return List.of();
        }

        // Lấy danh sách conversationIds
        List<String> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .toList();

        // Lấy tất cả messages từ các conversations (1 query)
        List<ChatMessage> messages = chatMessageRepository
                .findAllByConversationIdInOrderByCreatedDateDesc(conversationIds);

        return messages.stream()
                .map(msg -> toChatMessageResponse(msg, userId))
                .toList();
    }

    public ChatMessageResponse create(ChatMessageRequest request) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Validate: senderId phải khớp với user hiện tại
        if (!currentUserId.equals(request.getSenderId())) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Validate conversation và kiểm tra quyền
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // Tạo message
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

        // Gửi message qua WebSocket
        sendMessageViaWebSocket(chatMessage, conversation, currentUserId);

        return toChatMessageResponse(chatMessage, currentUserId);
    }

    /**
     * Gửi message qua WebSocket cho cả admin và user
     */
    private void sendMessageViaWebSocket(ChatMessage chatMessage, Conversation conversation, String senderId) {
        try {
            // Lấy userIds của 2 người: admin và user
            List<String> userIds = List.of(conversation.getAdminId(), conversation.getUserId());

            // Lấy WebSocket sessions của 2 người (1 query)
            Map<String, WebSocketSession> webSocketSessions = webSocketSessionRepository
                    .findAllByUserIdIn(userIds).stream()
                    .collect(Collectors.toMap(
                            WebSocketSession::getSocketSessionId,
                            Function.identity()
                    ));

            // Tạo response
            ChatMessageResponse response = toChatMessageResponse(chatMessage, senderId);

            // Gửi đến tất cả clients
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
                        log.error("Error sending message to client: {}", e.getMessage(), e);
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