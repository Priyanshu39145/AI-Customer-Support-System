package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

//    Conversation findByUserAndChatIdOrderByTimestampAsc(String userId, String chatId);

    List<Conversation> findByUserOrderByTimestampDesc(User user);

    boolean existsByIdAndUserId(String conversationId, String id);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId")
    int countMessagesInConversation(@Param("conversationId") String conversationId);
}