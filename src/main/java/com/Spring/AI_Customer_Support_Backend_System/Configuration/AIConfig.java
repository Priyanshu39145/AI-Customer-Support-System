package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AIConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String ollama_base_url;

    // Fast model for intent analysis and structured outputs
    @Bean(name = "fastChatClient")
    public ChatClient fastChatClient() {
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(ollama_base_url)
                .build();

        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model("llama3.2:3b")
                                .temperature(0.1)
                                .build()
                )
                .build();

        return ChatClient.builder(chatModel).build();
    }

    // Standard model for conversational responses
    @Bean(name = "conversationalChatClient")
    @Primary  // This is your default
    public ChatClient conversationalChatClient() {
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(ollama_base_url)
                .build();

        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model("llama3.1:latest")
                                .temperature(0.7)
                                .build()
                )
                .build();

        return ChatClient.builder(chatModel).build();
    }

}
