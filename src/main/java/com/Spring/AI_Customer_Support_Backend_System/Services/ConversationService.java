package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public List<ConversationDTO> getConversations(User user)   {
        List<Conversation> conversations = conversationRepository.findByUser(user);

        List<ConversationDTO> conversationDTOS = new ArrayList<>();
        for(Conversation conversation : conversations)  {
            ConversationDTO conversationDTO = new ConversationDTO(conversation.getId(),conversation.getTitle());
            conversationDTOS.add(conversationDTO);
        }

        return conversationDTOS;
    }
}
