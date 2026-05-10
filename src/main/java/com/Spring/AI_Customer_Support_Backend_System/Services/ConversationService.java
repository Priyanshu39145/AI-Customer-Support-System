package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.ConversationTitleRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ConversationStatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
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
        List<Conversation> conversations = conversationRepository.findByUserAndDeletedFalseOrderByTimestampDesc(user);

        return mapToConversationDTOs(conversations);
    }

    public List<ConversationDTO> searchConversations(User user, String keyword) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if(keyword == null || keyword.trim().isBlank()) {
            throw new IllegalArgumentException("Keyword is required");
        }

        String normalizedKeyword = keyword.trim();
        log.info("Searching conversations | userId: {}, keywordLength: {}", user.getId(), normalizedKeyword.length());
        return mapToConversationDTOs(conversationRepository.searchUserConversations(user, normalizedKeyword));
    }

    private List<ConversationDTO> mapToConversationDTOs(List<Conversation> conversations) {
        List<ConversationDTO> conversationDTOS = new ArrayList<>();
        for(Conversation conversation : conversations)  {
            ConversationDTO conversationDTO = new ConversationDTO(conversation.getId(),conversation.getTitle());
            conversationDTOS.add(conversationDTO);
        }

        return conversationDTOS;
    }

    public Conversation getConversationById(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if(conversation == null || conversation.isDeleted()) {
            return null;
        }
        return conversation;
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
                .status(ConversationStatusType.ACTIVE)
                .deleted(false)
                .build();
        conversationRepository.save(conversation);

        log.info("Conversation created | conversationId: {}, userId: {}, title: {}",
                conversation.getId(),
                user != null ? user.getId() : null,
                conversation.getTitle());
        return conversation;
    }

    @Transactional
    @CacheEvict(value = "conversations", key = "#user.id")
    public void deleteConversation(User user, String conversationId, boolean permanent) {
        Conversation conversation = getOwnedConversation(user, conversationId);
        log.info("Deleting conversation | conversationId: {}, userId: {}, permanent: {}",
                conversationId, user.getId(), permanent);

        if(permanent) {
            Ticket ticket = conversation.getTicket();
            if(ticket != null) {
                ticket.setConversation(null);
                conversation.setTicket(null);
                ticketRepository.save(ticket);
            }
            conversationRepository.delete(conversation);
            return;
        }

        conversation.setDeleted(true);
        conversation.setDeletedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    @Transactional
    @CacheEvict(value = "conversations", key = "#user.id")
    public ConversationDTO renameConversation(User user, String conversationId, ConversationTitleRequestDTO requestDTO) {
        Conversation conversation = getOwnedConversation(user, conversationId);
        String title = requestDTO.getTitle().trim();
        log.info("Renaming conversation | conversationId: {}, userId: {}", conversationId, user.getId());

        conversation.setTitle(title);
        conversationRepository.save(conversation);
        return new ConversationDTO(conversation.getId(), conversation.getTitle());
    }

    @Transactional
    @CacheEvict(value = "conversations", key = "#user.id")
    public ConversationDTO closeConversation(User user, String conversationId) {
        Conversation conversation = getOwnedConversation(user, conversationId);
        log.info("Closing conversation | conversationId: {}, userId: {}", conversationId, user.getId());

        if(conversation.getStatus() == ConversationStatusType.CLOSED) {
            throw new IllegalArgumentException("Conversation is already closed");
        }

        conversation.setStatus(ConversationStatusType.CLOSED);
        conversationRepository.save(conversation);
        return new ConversationDTO(conversation.getId(), conversation.getTitle());
    }

    private Conversation getOwnedConversation(User user, String conversationId) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if(conversation.isDeleted()) {
            throw new IllegalArgumentException("Conversation not found");
        }

        if(conversation.getUser() == null || !conversation.getUser().getId().equals(user.getId())) {
            log.warn("Conversation ownership validation failed | conversationId: {}, userId: {}",
                    conversationId, user.getId());
            throw new AccessDeniedException("Not allowed to access this conversation");
        }

        return conversation;
    }
}
