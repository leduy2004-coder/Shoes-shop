package com.java.chat_service.service;

import com.java.chat_service.dto.request.ConversationRequest;
import com.java.chat_service.dto.response.ConversationResponse;
import com.java.chat_service.entity.Conversation;
import com.java.chat_service.exception.AppException;
import com.java.chat_service.exception.ErrorCode;
import com.java.chat_service.repository.ConversationRepository;
import com.java.chat_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;

    public List<ConversationResponse> myConversations() {
        if (!GetInfo.isAdmin()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        String currentUserId = GetInfo.getLoggedInUserName();
        List<Conversation> conversations = conversationRepository.findAllByAdminId(currentUserId);

        return conversations.stream().map(this::toConversationResponse).toList();
    }

    public ConversationResponse create(ConversationRequest request) {
        // Validate: không cho tự chat với chính mình
        if (request.getSenderId().equals(request.getParticipantId())) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Validate: senderId phải khớp với user hiện tại
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUserId.equals(request.getSenderId())) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        boolean isAdmin = GetInfo.isAdmin();
        String adminId;
        String userId;

        if (isAdmin) {
            adminId = request.getSenderId();
            userId = request.getParticipantId();
        } else {
            adminId = request.getParticipantId();
            userId = request.getSenderId();
        }

        List<String> userIds = new ArrayList<>();
        userIds.add(userId);
        userIds.add(adminId);

        var sortedIds = userIds.stream().sorted().toList();
        String userIdHash = generateParticipantHash(sortedIds);

        var conversation = conversationRepository.findByParticipantsHash(userIdHash)
                .orElseGet(() -> {
                    // Chỉ có 2 người: user và admin
                    List<Conversation.ParticipantInfo> participantInfos = List.of(
                            Conversation.ParticipantInfo.builder()
                                    .userId(userId)
                                    .name(isAdmin ? request.getParticipantName() : request.getSenderName())
                                    .build(),
                            Conversation.ParticipantInfo.builder()
                                    .userId(adminId)
                                    .name(isAdmin ? request.getSenderName() : request.getParticipantName())
                                    .build()
                    );

                    // Build conversation info
                    Conversation newConversation = Conversation.builder()
                            .participantsHash(userIdHash)
                            .adminId(adminId) // Lưu adminId để dễ query
                            .participants(participantInfos)
                            .createdDate(Instant.now())
                            .modifiedDate(Instant.now())
                            .build();

                    return conversationRepository.save(newConversation);
                });

        return toConversationResponse(conversation);
    }

    public ConversationResponse getConversationById(String conversationId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // Kiểm tra user có trong conversation không
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isParticipant) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        return toConversationResponse(conversation);
    }

    private String generateParticipantHash(List<String> ids) {
        StringJoiner stringJoiner = new StringJoiner("_");
        ids.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        ConversationResponse conversationResponse = ConversationResponse.builder()
                .id(conversation.getId())
                .participantsHash(conversation.getParticipantsHash())
                .createdDate(conversation.getCreatedDate())
                .modifiedDate(conversation.getModifiedDate())
                .build();

        if (conversation.getParticipants() != null) {
            conversation.getParticipants().stream()
                    .filter(participantInfo -> !participantInfo.getUserId().equals(currentUserId))
                    .findFirst().ifPresent(participantInfo -> {
                        conversationResponse.setConversationName(participantInfo.getName());
                    });
        }

        return conversationResponse;
    }
}