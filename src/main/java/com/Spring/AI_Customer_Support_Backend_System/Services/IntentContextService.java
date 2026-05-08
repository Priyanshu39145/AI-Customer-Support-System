package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.IntentContextDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentContextService {

    private final TicketRepository ticketRepository;
    private final ConversationRepository conversationRepository;

    public IntentContextDTO buildContext(
            String currentMessage,
            User user,
            Conversation conversation,
            String messageHistory,
            boolean hasExistingTicket
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime hourStart = now.minusHours(1);

        // Count tickets created by this user
        int ticketsToday = ticketRepository.countByCreatedByAndCreatedAtAfter(user, todayStart);
        int ticketsThisHour = ticketRepository.countByCreatedByAndCreatedAtAfter(user, hourStart);

        // Get last ticket creation time
        LocalDateTime lastTicketTime = ticketRepository
                .findTopByCreatedByOrderByCreatedAtDesc(user)
                .map(Ticket::getCreatedAt)
                .orElse(null);

        // Count messages in this conversation (session)
        int messageCount = conversationRepository.countMessagesInConversation(conversation.getId());
        boolean isFirstMessage = messageCount <= 1; // 1 because user message is already saved

        return IntentContextDTO.builder()
                .userId(user.getId())
                .ticketsCreatedToday(ticketsToday)
                .ticketsCreatedThisHour(ticketsThisHour)
                .lastTicketCreatedAt(lastTicketTime)
                .messageCountInSession(messageCount)
                .sessionStartedAt(conversation.getTimestamp())
                .isFirstMessage(isFirstMessage)
                .hasExistingTicket(hasExistingTicket)
                .messageHistory(messageHistory)
                .currentMessage(currentMessage)
                .build();
    }
}