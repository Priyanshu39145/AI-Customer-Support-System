package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.MessageRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.MessageResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Message;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
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

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @CacheEvict(value = "ticketMessages", key = "#ticketId + '-' + #user.id")
    public MessageResponseDTO sendMessage(User user, String ticketId, MessageRequestDTO messageRequestDTO) {

        log.info("Sending message | userId: {}, ticketId: {}", user != null ? user.getId() : null, ticketId);

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(user==null || ticket==null) {
            log.error("User or Ticket not found | userId: {}, ticketId: {}", user != null ? user.getId() : null, ticketId);
            throw new IllegalArgumentException("User Or Ticker Not found");
        }
        else if(!user.getId().equals(ticket.getCreatedBy().getId()) && !user.getId().equals(ticket.getAssignedTo().getId())) {
            log.warn("Unauthorized message attempt | userId: {}, ticketId: {}", user.getId(), ticketId);
            throw new IllegalArgumentException("This User is not allowed in this chat");
        }

        //If the conversation opens and the ticket is still not in IN_PROGRESS make it
        if(ticket.getStatus() == StatusType.OPEN) {
            log.info("Auto-updating ticket status to IN_PROGRESS for ticketId: {}", ticketId);
            ticket.setStatus(StatusType.IN_PROGRESS);
        }

        Message message = Message.builder()
                .content(messageRequestDTO.getContent())
                .ticket(ticket)
                .sender(user)
                .build();

        messageRepository.save(message);
        //Not using modelMapper here
        log.info("Message saved successfully | messageId: {}", message.getId());
        return new MessageResponseDTO(message.getId(), message.getContent(), message.getSender().getId(), message.getCreatedAt());
    }

    @Cacheable(value = "ticketMessages", key = "#ticketId + '-' + #user.id")
    public List<MessageResponseDTO> getMessages(String ticketId, User user) {
        log.info("Fetching messages | ticketId: {}, userId: {}", ticketId, user != null ? user.getId() : null);
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(user==null || ticket==null) {
            log.error("User or Ticket not found | userId: {}, ticketId: {}", user != null ? user.getId() : null, ticketId);
            throw new IllegalArgumentException("User Or Ticker Not found");
        }
        else if(
                !user.getId().equals(ticket.getCreatedBy().getId()) &&
                        (ticket.getAssignedTo() == null ||
                                !user.getId().equals(ticket.getAssignedTo().getId()))
        ) {
            log.warn("Unauthorized access to messages | userId: {}, ticketId: {}", user.getId(), ticketId);
            throw new IllegalArgumentException("This User is not allowed in this chat");
        }

        if(ticket.getStatus() == StatusType.OPEN) {
            log.info("Auto-updating ticket status to IN_PROGRESS for ticketId: {}", ticketId);
            ticket.setStatus(StatusType.IN_PROGRESS);
        }

        List<Message> messages = messageRepository.findByTicketOrderByCreatedAtAsc(ticket);
        log.debug("Fetched {} messages for ticketId: {}", messages.size(), ticketId);
        return messages.stream().map(message -> new MessageResponseDTO(message.getId(), message.getContent(), message.getSender().getId(), message.getCreatedAt())).toList();
    }
}
