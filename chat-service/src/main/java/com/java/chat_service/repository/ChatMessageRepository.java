package com.java.chat_service.repository;

import com.java.chat_service.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findAllByConversationIdOrderByCreatedDateDesc(String conversationId);

    // Lấy tất cả messages của user từ conversations mà user tham gia
    @Query("{'conversationId' : { $in: ?0 }}")
    List<ChatMessage> findAllByConversationIdInOrderByCreatedDateDesc(List<String> conversationIds);
}