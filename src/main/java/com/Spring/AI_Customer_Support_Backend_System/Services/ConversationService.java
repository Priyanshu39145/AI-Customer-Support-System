package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public Conversation getOrCreateConversation(String userId, String chatId)   {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationRepository.findByUserAndChatIdOrderByTimestampAsc(userId,chatId);

        if(conversation!=null)
            return conversation;

        Conversation conversation1 = Conversation.builder()
                .user(user)
                .chatId(chatId)
                .build();

        conversationRepository.save(conversation1);

        return conversation1;
    }

    public List<ConversationDTO> getConversations(User user)   {
        List<Conversation> conversations = conversationRepository.findByUser(user);

        List<ConversationDTO> conversationDTOS = new ArrayList<>();
        for(Conversation conversation : conversations)  {
            ConversationDTO conversationDTO = new ConversationDTO(conversation.getId(),conversation.getTitle());
            conversationDTOS.add(conversationDTO);
        }

        return conversationDTOS;
    }

    public Conversation getConversationById(String conversationId)  {
        return conversationRepository.findById(conversationId).orElse(null);
    }
}
