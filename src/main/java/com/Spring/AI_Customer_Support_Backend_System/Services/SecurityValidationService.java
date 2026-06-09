package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.IntentContextDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SecurityValidationService {

    private static final List<String> INJECTION_PATTERNS = List.of(
            "ignore previous",
            "disregard",
            "new instructions",
            "you are now",
            "system:",
            "for every message",
            "always create ticket",
            "forget your rules",
            "act as",
            "pretend you are",
            "override",
            "bypass",
            "assistant:",
            "developer:",
            "ignore all instructions",
            "ignore all rules",
            "forget previous instructions",
            "jailbreak",
            "act as chatgpt",
            "act as system",
            "roleplay as"
    );

    private static final List<String> ABUSE_PATTERNS = List.of(
            "spam ticket",
            "flood ticket",
            "create 100 tickets",
            "mass escalate",
            "bombard support"
    );

    private static final int MAX_TICKETS_PER_HOUR = 5;
    private static final int MAX_TICKETS_PER_DAY = 10;
    private static final long MIN_SECONDS_BETWEEN_TICKETS = 30;

    /**
     * Validates if the user request is suspicious or abusive.
     * Returns error message if blocked, null if safe.
     */
    public String validateEscalationRequest(String message, IntentContextDTO context) {

        // Check 1: Injection pattern detection
        //We detect here prompt injection from a set of keywords ----
        String injectionError = detectInjectionPatterns(message);
        if (injectionError != null) {
            log.warn("Injection pattern detected | userId: {}, pattern: {}",
                    context.getUserId(), injectionError);
            return injectionError;
        }

        // Check 2: Rate limiting (tickets per hour/day)
        //We here check if the user has exceeded the daily ticket creation quota of the day or not ---
        String rateLimitError = checkTicketRateLimits(context);
        if (rateLimitError != null) {
            log.warn("Rate limit exceeded | userId: {}, ticketsToday: {}, ticketsThisHour: {}",
                    context.getUserId(),
                    context.getTicketsCreatedToday(),
                    context.getTicketsCreatedThisHour());
            return rateLimitError;
        }

        // Check 3: Ticket creation too soon after last ticket
        String timingError = checkTicketCreationTiming(context);
        if (timingError != null) {
            log.warn("Ticket created too quickly | userId: {}, secondsSinceLastTicket: {}",
                    context.getUserId(),
                    calculateSecondsSinceLastTicket(context));
            return timingError;
        }

        //If all security Checks passed --- then we return null ----
        return null; // All checks passed
    }

    private String detectInjectionPatterns(String message) {
        if (message == null) return null;

        String lowerMessage = message.toLowerCase();
        //If the message contains the keywords defined inside the INJECTION_PATTERNS --- then we say it contains suspicious content
        for (String pattern : INJECTION_PATTERNS) {
            if (lowerMessage.contains(pattern)) {
                return "Your message contains suspicious content. Please rephrase your request.";
            }
        }
        //If the message contains the keywords defined inside the ABUSE_PATTERNS --- then we say it contains abusive content
        for (String pattern : ABUSE_PATTERNS) {
            if (lowerMessage.contains(pattern)) {
                return "Your request appears abusive. Please describe the actual issue you need help with.";
            }
        }

        return null;
    }

    //If user has exceeded the hourly or daily ticket creation quota ---- then we say that the user has hit rate limit ---
    private String checkTicketRateLimits(IntentContextDTO context) {
        if (context.getTicketsCreatedThisHour() >= MAX_TICKETS_PER_HOUR) {
            return "You've created too many tickets recently. Please wait before creating another ticket.";
        }

        if (context.getTicketsCreatedToday() >= MAX_TICKETS_PER_DAY) {
            return "Daily ticket limit reached. Please try again tomorrow or contact support directly.";
        }

        return null;
    }

    //We get the time of the last ticket created by the user ---- and then find the seconds after which the new ticket is supposed to be created ---
    private String checkTicketCreationTiming(IntentContextDTO context) {
        if (context.getLastTicketCreatedAt() == null) {
            return null; // First ticket ever, allow it
        }

        long secondsSinceLastTicket = calculateSecondsSinceLastTicket(context);
        //If
        if (secondsSinceLastTicket < MIN_SECONDS_BETWEEN_TICKETS) {
            return "Please wait a moment before creating another ticket.";
        }

        return null;
    }

    private long calculateSecondsSinceLastTicket(IntentContextDTO context) {
        if (context.getLastTicketCreatedAt() == null) return Long.MAX_VALUE;

        return Duration.between(
                context.getLastTicketCreatedAt(),
                LocalDateTime.now()
        ).getSeconds();
    }
}
