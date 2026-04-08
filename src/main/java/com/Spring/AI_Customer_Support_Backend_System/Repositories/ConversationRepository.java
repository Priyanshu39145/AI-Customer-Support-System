package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    Conversation findByUserAndChatIdOrderByTimestampAsc(String userId, String chatId);

    List<Conversation> findByUser(User user);
}