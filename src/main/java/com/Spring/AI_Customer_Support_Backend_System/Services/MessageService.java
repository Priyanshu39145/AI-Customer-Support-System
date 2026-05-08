package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.MessageRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.MessageResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Message;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.SenderType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.MessageRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final ConversationRepository conversationRepository;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
//
//    @CacheEvict(value = "ticketMessages", key = "#ticketId + '-' + #user.id")
//    public MessageResponseDTO sendMessage(User user, String ticketId, MessageRequestDTO messageRequestDTO) {
//
//        log.info("Sending message | userId: {}, ticketId: {}", user != null ? user.getId() : null, ticketId);
//
//        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
//        if(user==null || ticket==null) {
//            log.error("User or Ticket not found | userId: {}, ticketId: {}", user != null ? user.getId() : null, ticketId);
//            throw new IllegalArgumentException("User Or Ticker Not found");
//        }
//        else if(!user.getId().equals(ticket.getCreatedBy().getId()) && !user.getId().equals(ticket.getAssignedTo().getId())) {
//            log.warn("Unauthorized message attempt | userId: {}, ticketId: {}", user.getId(), ticketId);
//            throw new IllegalArgumentException("This User is not allowed in this chat");
//        }
//
//        //If the conversation opens and the ticket is still not in IN_PROGRESS make it
//        if(ticket.getStatus() == StatusType.OPEN) {
//            log.info("Auto-updating ticket status to IN_PROGRESS for ticketId: {}", ticketId);
//            ticket.setStatus(StatusType.IN_PROGRESS);
//        }
//
//        Message message = Message.builder()
//                .content(messageRequestDTO.getContent())
//                .ticket(ticket)
//                .sender(user)
//                .build();
//
//        messageRepository.save(message);
//        //Not using modelMapper here
//        log.info("Message saved successfully | messageId: {}", message.getId());
//        return new MessageResponseDTO(message.getId(), message.getContent(), message.getSender().getId(), message.getCreatedAt());
//    }
//
    //Gets all the messages inside a conversation ---
    //Caching is done using the conversation id and user id ---
    @Cacheable(value = "conversationMessages",
            key = "#conversationId + '_' + #user.id")
    public List<MessageResponseDTO> getMessages(String conversationId, User user) {

        log.info("Fetching messages | conversationId: {}, userId: {}",
                conversationId, user != null ? user.getId() : null);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        //We check if the conversation exists inside the DB in the particular id and the user
        boolean isValid = conversationRepository
                .existsByIdAndUserId(conversationId, user.getId());

        if (!isValid) {
            log.warn("Unauthorized access | userId: {}, conversationId: {}", user.getId(), conversationId);
            throw new IllegalArgumentException("Unauthorized access");
        }


        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        log.debug("Fetched {} messages for conversationId: {}", messages.size(), conversationId);


        return messages.stream()
                .map(m -> new MessageResponseDTO(
                        m.getId(),
                        m.getContent(),
                        m.getSender(), //this is of SenderType ---- The frontend will handle other things
                        m.getSenderUser().getId(),
                        m.getCreatedAt()
                ))
                .toList();
    }

    //This create message is used when we chat in AI --- it stores the userMessage and AIReply ----
    //We use CacheEvict to update the Cache whenever there is a change inside the messageDTOs
    @CacheEvict(
            value = "conversationMessages",
            key = "#conversation.id + '_' + #sender.id"
    )
    public Message createMessage(String content, Conversation conversation, SenderType senderType, User sender) {
        log.info("Persisting conversation message | conversationId: {}, senderType: {}, senderUserId: {}, contentLength: {}",
                conversation != null ? conversation.getId() : null,
                senderType,
                sender != null ? sender.getId() : null,
                content != null ? content.length() : 0);

        Message message = Message.builder()
                .content(content)
                .conversation(conversation)
                .sender(senderType)
                .senderUser(sender)
                .build();

        messageRepository.save(message);

        log.debug("Conversation message saved | messageId: {}, conversationId: {}",
                message.getId(),
                conversation != null ? conversation.getId() : null);

        return message;
    }



}
