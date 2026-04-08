package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AIResponse;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public AIResponse askAI(String message) {
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        AIResponse response = new AIResponse(message,content, LocalDateTime.now());
        return response;
    }


    public AIResponse askAIWithHistory(String message, StringBuilder history) {
        String sys = "You are an intelligent AI customer support assistant designed to help users resolve their queries efficiently and conversationally. "
                + "You must carefully understand the user’s intent using both the current message and the provided conversation history, and respond in a clear, helpful, and professional manner. "
                + "Always maintain context from previous messages to ensure continuity in the conversation. "
                + "If the user’s issue can be resolved directly, provide a precise and actionable solution. "
                + "If the issue is unclear, ask relevant follow-up questions. "
                + "If the request indicates frustration, urgency, or a problem that cannot be resolved through automated assistance, you should recognize this and prepare to escalate by suggesting ticket creation. "
                + "Keep responses concise but informative, avoid unnecessary verbosity, and ensure a natural, human-like conversational tone.\n\n"
                + "Conversation History:\n"
                + history + "\n\n";
        String content = chatClient.prompt()
                .system(sys)
                .user(message)
                .call()
                .content();

        return new AIResponse(message, content, LocalDateTime.now());
    }

    public String generateTitle(String message) {

        String prompt = "Summarize this customer query in 3-4 words only, no extra text:\n" + message;

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
