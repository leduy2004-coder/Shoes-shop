package com.java.chat_service.repository;

import com.java.chat_service.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByParticipantsHash(String hash);

    @Query("{'participants.userId' : ?0}")
    List<Conversation> findAllByParticipantIdsContains(String userId);

    // Tìm tất cả conversations của admin với các user khác nhau
    @Query("{'adminId' : ?0}")
    List<Conversation> findAllByAdminId(String adminId);
}