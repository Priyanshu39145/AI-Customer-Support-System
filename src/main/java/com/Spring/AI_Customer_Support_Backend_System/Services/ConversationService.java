package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatClient ollamaChatClient;

//    public Conversation getOrCreateConversation(String userId, String chatId)   {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Conversation conversation = conversationRepository.findByUserAndChatIdOrderByTimestampAsc(userId,chatId);
//
//        if(conversation!=null)
//            return conversation;
//
//        Conversation conversation1 = Conversation.builder()
//                .user(user)
//                .build();
//
//        conversationRepository.save(conversation1);
//
//        return conversation1;
//    }

    @Cacheable(value = "conversations", key = "#user.id")
    public List<ConversationDTO> getConversations(User user)   {
        List<Conversation> conversations = conversationRepository.findByUserOrderByTimestampDesc(user);

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

    @CacheEvict(value = "conversations", key = "#user.id")
    public Conversation createConversation(User user, String message) {
        log.info("Creating conversation | userId: {}, initialMessageLength: {}",
                user != null ? user.getId() : null,
                message != null ? message.length() : 0);

        String title = ollamaChatClient.prompt()
                .system("You should convert the given message into a meaningful and short conversation title of max 5 words only")
                .user(message)
                .call()
                .content();

        if(title.length()>50)
            title = title.substring(0,49);

        Conversation conversation = Conversation.builder()
                .title(title)
                .user(user)
                .build();
        conversationRepository.save(conversation);

        log.info("Conversation created | conversationId: {}, userId: {}, title: {}",
                conversation.getId(),
                user != null ? user.getId() : null,
                conversation.getTitle());
        return conversation;
    }
}
