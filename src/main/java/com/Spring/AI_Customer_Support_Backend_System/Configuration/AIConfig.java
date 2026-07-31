package com.Spring.AI_Customer_Support_Backend_System.Configuration;


import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class AIConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String ollama_base_url;


    //Before it the OllamaApi used the default RestClient builder to request to ollama for a response
    //Now we are giving it a timeout for all the responses ---- using JdkClientHttpRequestFactory ----
    //We add this timeOutBoundedRestClientBuilder to both the chat client ollamaApis ----

    // Local Ollama inference can legitimately take 20-30s; this bounds it so a stuck
    // request fails fast instead of blocking a thread (and, previously, a DB connection) forever.
    private RestClient.Builder timeoutBoundedRestClientBuilder() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build()
        );
        requestFactory.setReadTimeout(Duration.ofSeconds(45));
        return RestClient.builder().requestFactory(requestFactory);
    }

    // Fast model for intent analysis and structured outputs
    @Bean(name = "fastChatClient")
    public ChatClient fastChatClient() {
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(ollama_base_url)
                .restClientBuilder(timeoutBoundedRestClientBuilder())
                .build();

        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model("llama3.2:3b")
                                .temperature(0.1) //temperature indicates the variety of responses ---- lesser temperature more robust the response
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
                .restClientBuilder(timeoutBoundedRestClientBuilder())
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
//Done
