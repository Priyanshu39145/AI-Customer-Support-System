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

    public Conversation getOrCreateConversation(String userId, String chatId)   {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationRepository.findByUserAndChatIdOrderByTimestampAsc(userId,chatId);

        if(conversation!=null)
            return conversation;

        Conversation conversation1 = Conversation.builder()
                .user(user)
                .chatId(chatId)
                .build();

        conversationRepository.save(conversation1);

        return conversation1;
    }
}
