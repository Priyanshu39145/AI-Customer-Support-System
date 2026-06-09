package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.MessageRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.MessageResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Services.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;


    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable String conversationId)  {
        //This statement gives us all the details of the current user including the role ---
        //We have to set /doctors as request matchers to only roles containing doctor ---
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(messageService.getMessages(conversationId,user));
    }
    //It shows all the messages --- of the User and the AI inside the chat interface --- of an existing conversation ----
}
