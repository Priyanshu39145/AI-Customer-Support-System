package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketAnalysisDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolService {

//    private final ChatClient ollamaChatClient;
    private final VectorStore vectorStore;
    private final ConversationRepository conversationRepository;
    private final TicketService ticketService;

    private final AIService aiService;

    @Cacheable(
            value = "policySearch",
            key = "#query",
            unless = "#result == null || #result.contains('No relevant')"
    )
    @Tool(name = "searchCompanyPolicy", description = "Retrieve official company policies with citations")
    public String searchCompanyPolicy(String query) {
        log.info("Searching company policy | queryLength: {}",
                query != null ? query.length() : 0);

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .similarityThreshold(0.7)
                        .build()
        );

        if (documents.isEmpty()) {
            log.info("Company policy search returned no results");
            return "No relevant company policy found.";
        }

        log.info("Company policy search completed | matches: {}", documents.size());
        return documents.stream()
                .map(doc -> {
                    String content = doc.getFormattedContent();

                    Map<String, Object> meta = doc.getMetadata();

                    String source = (String) meta.getOrDefault("source", "unknown");
                    String page = String.valueOf(meta.getOrDefault("page", "N/A"));

                    return content + "\n(Source: " + source + ", Page: " + page + ")";
                })
                .collect(Collectors.joining("\n\n"));
    }


    @Tool(name = "createSupportTicket", description = "Deterministically creates one support ticket for the current conversation. Backend validates ownership and prevents duplicate conversation tickets." , returnDirect = true)
    public Map<String,Object> createSupportTicket(String userMessage, String conversationId) {
        log.info("Creating support ticket from chat flow | conversationId: {}, messageLength: {}",
                conversationId,
                userMessage != null ? userMessage.length() : 0);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        User user = getCurrentUser();
        if (!conversation.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized ticket creation attempt | conversationId: {}, userId: {}",
                    conversationId,
                    user.getId());
            throw new RuntimeException("Unauthorized access to conversation");
        }
        try {

            // Validation 1: Message length
            if (userMessage == null || userMessage.trim().length() < 20) {
                log.info("Ticket creation rejected: message too short | conversationId: {}, length: {}",
                        conversationId,
                        userMessage != null ? userMessage.length() : 0);
                return Map.of(
                        "type", "ERROR",
                        "value", "Please provide more details about your issue (at least 20 characters)."
                );
            }

// Validation 2: Not just repetitive text
            String trimmed = userMessage.trim().toLowerCase();
            if (isRepetitiveText(trimmed)) {
                log.info("Ticket creation rejected: repetitive text detected | conversationId: {}", conversationId);
                return Map.of(
                        "type", "ERROR",
                        "value", "Please describe your actual issue clearly."
                );
            }

// Validation 3: Contains meaningful words
            if (!containsMeaningfulContent(trimmed)) {
                log.info("Ticket creation rejected: no meaningful content | conversationId: {}", conversationId);
                return Map.of(
                        "type", "ERROR",
                        "value", "Please describe your issue with more specific details."
                );
            }
            if (conversation.getTicket() != null) {
                log.info("Duplicate ticket prevented in tool layer | conversationId: {}, ticketId: {}",
                        conversationId,
                        conversation.getTicket().getId());
                return Map.of(
                        "type", "DUPLICATE_TICKET",
                        "value", "This conversation already has a support ticket.",
                        "ticketId", conversation.getTicket().getId()
                );
            }

            TicketAnalysisDTO analysis = aiService.analyzeTicketDetails(userMessage);



            CreateTicketResponseDTO ticket = ticketService.createTicket(
                    user,
                    new CreateTicketRequestDTO(analysis.getTitle(), userMessage),
                    conversation,
                    analysis.getPriority(),
                    analysis.getCategory()
            );
            log.info("Support ticket created from chat flow | conversationId: {}, ticketId: {}, priority: {}, category: {}",
                    conversationId,
                    ticket.getId(),
                    analysis.getPriority(),
                    analysis.getCategory());
            return Map.of(
                    "type", "CREATE_TICKET",
                    "value", "Ticket created successfully with ID: " + ticket.getId(),
                    "priority", analysis.getPriority().name(),
                    "category", analysis.getCategory().name(),
                    "reason", analysis.getReason()
            );

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Conversation already has a ticket")) {
                log.info("Duplicate ticket blocked by backend validation | conversationId: {}", conversationId);
                return Map.of(
                        "type", "DUPLICATE_TICKET",
                        "value", "This conversation already has a support ticket."
                );
            }

            log.error("Ticket creation failed | conversationId: {}, error: {}", conversationId, e.getMessage());
            return Map.of(
                    "type", "ERROR",
                    "value", "Something went wrong while creating your ticket."
            );

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Database constraint violation (race condition caught)
            log.warn("Race condition: duplicate ticket prevented by database constraint | conversationId: {}", conversationId);
            return Map.of(
                    "type", "DUPLICATE_TICKET",
                    "value", "This conversation already has a support ticket."
            );
        }
    }






    @Tool(
            name = "getTicketDetails",
            description = "Fetch full details of the support ticket for a given conversation." +
                    "Returns structured ticket data in JSON format."
    )
    public Map<String, Object> getTicketDetails(String conversationId) {
        log.info("Fetching support ticket details from chat flow | conversationId: {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)

                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User currentUser = getCurrentUser();

        if (!conversation.getUser().getId().equals(currentUser.getId())) {
            log.warn("Unauthorized ticket details access attempt | conversationId: {}, userId: {}",
                    conversationId,
                    currentUser.getId());

            throw new RuntimeException("Unauthorized access to conversation");

        }



        Ticket ticket = conversation.getTicket();

        if (ticket == null) {
            log.info("No ticket found for conversation | conversationId: {}", conversationId);
            return Map.of(
                    "status", "NOT_FOUND",
                    "message", "No ticket found for this conversation"
            );
        }

        log.info("Ticket details fetched | conversationId: {}, ticketId: {}, status: {}",
                conversationId,
                ticket.getId(),
                ticket.getStatus());
        return Map.of(
                "ticketId", ticket.getId(),
                "status", ticket.getStatus().toString(),
                "priority", ticket.getPriority().toString(),
                "category", ticket.getCategory().toString(),
                "assignedAgent", ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getName()
                        : "Not assigned yet",
                "createdAt", ticket.getCreatedAt().toString(),
                "description", ticket.getDescription(),
                "lastUpdated" , ticket.getCreatedAt().toString()
        );
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private boolean isRepetitiveText(String text) {
        // Check if text is same word/phrase repeated
        String[] words = text.split("\\s+");
        if (words.length < 3) return false;

        // If 80%+ of words are identical, it's repetitive
        String firstWord = words[0];
        long sameWordCount = java.util.Arrays.stream(words)
                .filter(w -> w.equals(firstWord))
                .count();

        return (double) sameWordCount / words.length > 0.8;
    }

    private boolean containsMeaningfulContent(String text) {
        // Check for common support-related keywords
        List<String> meaningfulKeywords = List.of(
                "payment", "account", "error", "issue", "problem", "help",
                "not working", "can't", "cannot", "unable", "failed",
                "refund", "charged", "bug", "broken", "locked", "blocked"
        );

        String lowerText = text.toLowerCase();
        boolean hasKeyword =
                meaningfulKeywords.stream()
                        .anyMatch(lowerText::contains);

        boolean enoughWords =
                text.trim().split("\\s+").length >= 5;

        return hasKeyword || enoughWords;
    }
}
