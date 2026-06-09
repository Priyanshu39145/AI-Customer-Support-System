package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

//    Conversation findByUserAndChatIdOrderByTimestampAsc(String userId, String chatId);

    List<Conversation> findByUserAndDeletedFalseOrderByTimestampDesc(User user);

    boolean existsByIdAndUserId(String conversationId, String id);

    boolean existsByIdAndUserIdAndDeletedFalse(String conversationId, String id);

    @Query("""
            SELECT DISTINCT c
            FROM Conversation c
            LEFT JOIN c.messages m
            WHERE c.user = :user
              AND c.deleted = false
              AND (
                  LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY c.timestamp DESC
            """)
    /*
    searches all non-deleted conversations of a particular user where either:
    - the conversation title contains the keyword
    OR
    - any message inside the conversation contains the keyword
     */
    List<Conversation> searchUserConversations(@Param("user") User user,
                                               @Param("keyword") String keyword);


    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId")
    int countMessagesInConversation(@Param("conversationId") String conversationId);
}
