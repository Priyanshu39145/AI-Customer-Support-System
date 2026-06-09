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
    //Whenever the user goes to the Ai Chat --- the User sees the done conversations in a nav bar ---
    //This endpoint gives us all the conversations of the user in a DTO format for showing in the nav bar ---
    //See the implementation in Service
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations()  {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.getConversations(user));
    }

    //We have a search bar to search for conversations --- we have made it available to the user
    // for searching the conversations by keyword
    //See the Service method ---
    @GetMapping("/conversations/search")
    public ResponseEntity<List<ConversationDTO>> searchConversations(@RequestParam String keyword) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.searchConversations(user, keyword));
    }

    //Using this endpoint we can rename the conversations --- usually we rename the conversation title which gets displayed in the nav bar ---
    @PutMapping("/conversations/{conversationId}/title")
    public ResponseEntity<ConversationDTO> renameConversation(@PathVariable String conversationId,
                                                              @Valid @RequestBody ConversationTitleRequestDTO requestDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.renameConversation(user, conversationId, requestDTO));
    }

    //This helps us to close a conversation --- making it inactive ----
    @PutMapping("/conversations/{conversationId}/close")
    public ResponseEntity<ConversationDTO> closeConversation(@PathVariable String conversationId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(conversationService.closeConversation(user, conversationId));
    }

    //This endpoint is used to delete a conversation ---
    //We can either delete the conversation permanently --- permanent flag is true then --- the conversation gets deleted from the database ---
    //If we dont wanna delete permanently --- then we just set delete flag true ---
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId,
                                                   @RequestParam(defaultValue = "false") boolean permanent) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        conversationService.deleteConversation(user, conversationId, permanent);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
