package com.Spring.AI_Customer_Support_Backend_System.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntentContextDTO {
    // User behavior context
    private String userId;
    private int ticketsCreatedToday;
    private int ticketsCreatedThisHour;
    private LocalDateTime lastTicketCreatedAt;

    // Session context
    private int messageCountInSession;
    private LocalDateTime sessionStartedAt;
    private boolean isFirstMessage;

    // Conversation context
    private boolean hasExistingTicket;
    private String messageHistory;
    private String currentMessage;
}
