package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;

    public AIResponse askAI(String message) {
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        AIResponse response = new AIResponse(message,content, LocalDateTime.now());
        return response;
    }
}
