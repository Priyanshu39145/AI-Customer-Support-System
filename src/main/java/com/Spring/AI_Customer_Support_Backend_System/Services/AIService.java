package com.Spring.AI_Customer_Support_Backend_System.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;

    public String askAI(String message) {

        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
