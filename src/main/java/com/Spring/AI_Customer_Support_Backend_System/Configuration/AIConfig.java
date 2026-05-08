package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                  ChatModel chatModel) {

        return builder
                .defaultSystem("You are a helpful AI assistant")
                .build();
    }
}
