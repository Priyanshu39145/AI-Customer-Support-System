package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AIResponse;
import com.Spring.AI_Customer_Support_Backend_System.Services.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/test")
    public AIResponse test(@RequestParam String message) {
        return aiService.askAI(message);
    }
}
