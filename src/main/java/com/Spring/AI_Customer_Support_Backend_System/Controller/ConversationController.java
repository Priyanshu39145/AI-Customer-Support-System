package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationTitleRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Services.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/conversations/search")
    public ResponseEntity<List<ConversationDTO>> searchConversations(@RequestParam String keyword) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.searchConversations(user, keyword));
    }

    @PutMapping("/conversations/{conversationId}/title")
    public ResponseEntity<ConversationDTO> renameConversation(@PathVariable String conversationId,
                                                              @Valid @RequestBody ConversationTitleRequestDTO requestDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.renameConversation(user, conversationId, requestDTO));
    }

    @PutMapping("/conversations/{conversationId}/close")
    public ResponseEntity<ConversationDTO> closeConversation(@PathVariable String conversationId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.closeConversation(user, conversationId));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId,
                                                   @RequestParam(defaultValue = "false") boolean permanent) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        conversationService.deleteConversation(user, conversationId, permanent);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
