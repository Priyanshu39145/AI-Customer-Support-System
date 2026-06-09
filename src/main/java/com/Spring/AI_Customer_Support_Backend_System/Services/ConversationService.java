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


    @Cacheable(value = "conversations", key = "#user.id")
    public List<ConversationDTO> getConversations(User user)   {
        //We find the conversations from the database by latest time first and not deleted conversations ----
        //Then we convert the entities into DTOS ----
        List<Conversation> conversations = conversationRepository.findByUserAndDeletedFalseOrderByTimestampDesc(user);

        return mapToConversationDTOs(conversations);
    }

    @Cacheable(
            value = "conversationSearch",
            key = "#user.id + ':' + #keyword"
    )
    public List<ConversationDTO> searchConversations(User user, String keyword) {
        //If user is null then we throw error that Auth required
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }
        //If there is no keyword --- we ask for keyword ---
        if(keyword == null || keyword.trim().isBlank()) {
            throw new IllegalArgumentException("Keyword is required");
        }

        String normalizedKeyword = keyword.trim();
        log.info("Searching conversations | userId: {}, keywordLength: {}", user.getId(), normalizedKeyword.length());
        //We search using Query Method inside JPA Repository ---- see there ---
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

    //We just delete the conversation here ---
    @Transactional
    @CacheEvict(value = "conversations", key = "#user.id")
    public void deleteConversation(User user, String conversationId, boolean permanent) {
        Conversation conversation = getOwnedConversation(user, conversationId);
        log.info("Deleting conversation | conversationId: {}, userId: {}, permanent: {}",
                conversationId, user.getId(), permanent);
        //If permanent flag true --- then delete from DB --- otherwise just set delete flag true ---
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
        //We get the conversation of a particular user ---- using the id ---
        Conversation conversation = getOwnedConversation(user, conversationId);
        //We get the title from the new given title ---
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
        //If the conversation is already closed --- we cant close it again ---
        if(conversation.getStatus() == ConversationStatusType.CLOSED) {
            throw new IllegalArgumentException("Conversation is already closed");
        }

        //We set the CLOSED status and save ---
        conversation.setStatus(ConversationStatusType.CLOSED);
        conversationRepository.save(conversation);
        return new ConversationDTO(conversation.getId(), conversation.getTitle());
    }

    //Using this method --- we find the the correct conversation of a user using the id ---
    //We first check for Auth --- then fetch the conversation --- then check if the conversation is deleted or not --
    //Then we check if the conversation fetched has the same user as the given user ---- then we send the conversation ---
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
