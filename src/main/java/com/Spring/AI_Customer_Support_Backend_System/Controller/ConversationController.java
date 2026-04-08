package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations()  {
        //This statement gives us all the details of the current user including the role ---
        //We have to set /doctors as request matchers to only roles containing doctor ---
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.getConversations(user));
    }
}
