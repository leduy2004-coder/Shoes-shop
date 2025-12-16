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
import com.java.chat_service.utility.GetInfo;
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

            log.info("WebSocketSession created with id: {} for user: {}",
                    webSocketSession.getId(), introspectResponse.getUserId());
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