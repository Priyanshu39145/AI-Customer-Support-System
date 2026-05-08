package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AIResponse;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping({"/chat", "/chat/{conversationId}"})
    public ResponseEntity<AIResponse> chat(@RequestBody String message, @PathVariable(required = false) String conversationId) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        log.info("Received chat request | userId: {}, conversationId: {}, messageLength: {}",
                user.getId(),
                conversationId,
                message != null ? message.length() : 0);

        return ResponseEntity.ok(chatService.chat(message,user,conversationId));
    }

}
