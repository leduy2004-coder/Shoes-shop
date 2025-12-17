package com.java.chat_service.service;

import com.java.chat_service.dto.request.ConversationRequest;
import com.java.chat_service.dto.response.ConversationResponse;
import com.java.chat_service.entity.Conversation;
import com.java.chat_service.exception.AppException;
import com.java.chat_service.exception.ErrorCode;
import com.java.chat_service.repository.ConversationRepository;
import com.java.chat_service.repository.feignClient.IdentityClient;
import com.java.chat_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;
    IdentityClient identityClient;

    public List<ConversationResponse> myConversations() {
        String currentUserId = GetInfo.getLoggedInUserName();
        if (currentUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (GetInfo.isAdmin()) {
            List<Conversation> conversations = conversationRepository
                    .findAllByAdminIdOrderByModifiedDateDesc(currentUserId);

            return conversations.stream()
                    .map(this::toConversationResponseForAdmin)
                    .toList();
        } else {
            Conversation conversation = conversationRepository
                    .findByUserId(currentUserId)
                    .orElse(null);

            if (conversation == null) {
                return List.of();
            }

            return List.of(toConversationResponseForUser(conversation));
        }
    }

    public ConversationResponse create(ConversationRequest request) {
        boolean isAdmin = GetInfo.isAdmin();

        String userId = request.getParticipantId();
        String userName = request.getParticipantName();

        Conversation conversation = conversationRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .userId(userId)
                            .name(userName)
                            .adminId(identityClient.getAdmin().getResult().getId())
                            .createdDate(Instant.now())
                            .modifiedDate(Instant.now())
                            .build();

                    return conversationRepository.save(newConversation);
                });

        return isAdmin
                ? toConversationResponseForAdmin(conversation)
                : toConversationResponseForUser(conversation);
    }

    private ConversationResponse toConversationResponseForAdmin(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .conversationName(conversation.getName())
                .createdDate(conversation.getCreatedDate())
                .modifiedDate(conversation.getModifiedDate())
                .build();
    }

    private ConversationResponse toConversationResponseForUser(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .conversationName("Admin")
                .createdDate(conversation.getCreatedDate())
                .modifiedDate(conversation.getModifiedDate())
                .build();
    }
}