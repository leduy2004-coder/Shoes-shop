package com.java.chat_service.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.java.IntrospectRequest;
import com.java.chat_service.dto.request.ChatMessageRequest;
import com.java.chat_service.dto.response.ChatMessageResponse;
import com.java.chat_service.entity.Conversation;
import com.java.chat_service.entity.WebSocketSession;
import com.java.chat_service.repository.ConversationRepository;
import com.java.chat_service.service.ChatMessageService;
import com.java.chat_service.service.IdentityService;
import com.java.chat_service.service.WebSocketSessionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer server;
    IdentityService identityService;
    WebSocketSessionService webSocketSessionService;
    ChatMessageService chatMessageService;
    ConversationRepository conversationRepository;

    @OnConnect
    public void clientConnected(SocketIOClient client) {
        // Get Token from request param
        String token = client.getHandshakeData().getSingleUrlParam("token");

        // Verify token
        var introspectResponse = identityService.introspect(IntrospectRequest.builder()
                .token(token)
                .build());

        // If Token is invalid disconnect
        if (introspectResponse.isValid()) {
            log.info("Client connected: {}", client.getSessionId());
            // Persist webSocketSession
            WebSocketSession webSocketSession = WebSocketSession.builder()
                    .socketSessionId(client.getSessionId().toString())
                    .userId(introspectResponse.getUserId())
                    .createdAt(Instant.now())
                    .build();
            webSocketSession = webSocketSessionService.create(webSocketSession);

            // Join room với userId để dễ dàng gửi message
            client.joinRoom(introspectResponse.getUserId());

            log.info("WebSocketSession created with id: {} for user: {}", webSocketSession.getId(), introspectResponse.getUserId());
        } else {
            log.error("Authentication fail: {}", client.getSessionId());
            client.disconnect();
        }
    }

    @OnDisconnect
    public void clientDisconnected(SocketIOClient client) {
        log.info("Client disConnected: {}", client.getSessionId());
        webSocketSessionService.deleteSession(client.getSessionId().toString());
    }

    @OnEvent("send_message")
    public void onSendMessage(SocketIOClient client, ChatMessageRequest request) {
        try {
            // Lấy userId từ session
            WebSocketSession session = webSocketSessionService.findBySocketSessionId(client.getSessionId().toString());
            if (session == null) {
                log.error("Session not found for client: {}", client.getSessionId());
                client.sendEvent("error", "Session not found");
                return;
            }

            String senderId = session.getUserId();

            // Validate: senderId phải khớp với session
            if (!senderId.equals(request.getSenderId())) {
                log.error("SenderId mismatch: session={}, request={}", senderId, request.getSenderId());
                client.sendEvent("error", "SenderId mismatch");
                return;
            }

            // Kiểm tra conversation tồn tại và user có trong conversation
            Conversation conversation = conversationRepository.findById(request.getConversationId())
                    .orElse(null);

            if (conversation == null) {
                log.error("Conversation not found: {}", request.getConversationId());
                client.sendEvent("error", "Conversation not found");
                return;
            }

            // Validate: chỉ có 2 người trong conversation
            if (conversation.getParticipants().size() != 2) {
                log.error("Conversation must have exactly 2 participants: {}", request.getConversationId());
                client.sendEvent("error", "Invalid conversation");
                return;
            }

            // Kiểm tra user có trong conversation không
            boolean isParticipant = conversation.getParticipants().stream()
                    .anyMatch(p -> p.getUserId().equals(senderId));

            if (!isParticipant) {
                log.error("User {} is not a participant in conversation {}", senderId, request.getConversationId());
                client.sendEvent("error", "Not a participant");
                return;
            }

            // Nếu client không gửi senderName, lấy từ conversation
            if (request.getSenderName() == null || request.getSenderName().isEmpty()) {
                String senderName = conversation.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(senderId))
                        .findFirst()
                        .map(Conversation.ParticipantInfo::getName)
                        .orElse(senderId);
                request.setSenderName(senderName);
            }

            // Lưu message vào database và gửi qua WebSocket
            ChatMessageResponse messageResponse = chatMessageService.create(request);

            // Gửi confirmation về cho sender
            client.sendEvent("message_sent", messageResponse);
            log.info("Message sent successfully from user: {} in conversation: {}", senderId, request.getConversationId());

        } catch (Exception e) {
            log.error("Error handling send_message event: {}", e.getMessage(), e);
            client.sendEvent("error", "Failed to send message: " + e.getMessage());
        }
    }

    @PostConstruct
    public void startServer() {
        server.start();
        server.addListeners(this);
        log.info("Socket server started");
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket server stopped");
    }
}