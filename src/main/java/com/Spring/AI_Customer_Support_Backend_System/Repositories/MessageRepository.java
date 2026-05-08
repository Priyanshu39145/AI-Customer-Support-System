package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Message;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {

    List<Message> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}