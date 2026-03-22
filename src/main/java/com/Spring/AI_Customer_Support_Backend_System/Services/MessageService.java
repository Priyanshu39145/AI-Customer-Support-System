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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public MessageResponseDTO sendMessage(User user, String ticketId, MessageRequestDTO messageRequestDTO) {


        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(user==null || ticket==null)
            throw new IllegalArgumentException("User Or Ticker Not found");
        else if(!user.getId().equals(ticket.getCreatedBy().getId()) && !user.getId().equals(ticket.getAssignedTo().getId()))
            throw new IllegalArgumentException("This User is not allowed in this chat");

        //If the conversation opens and the ticket is still not in IN_PROGRESS make it
        if(ticket.getStatus() == StatusType.OPEN) {
            ticket.setStatus(StatusType.IN_PROGRESS);
        }

        Message message = Message.builder()
                .content(messageRequestDTO.getContent())
                .ticket(ticket)
                .sender(user)
                .build();

        messageRepository.save(message);
        //Not using modelMapper here
        return new MessageResponseDTO(message.getId(), message.getContent(), message.getSender().getId(), message.getCreatedAt());
    }

    public List<MessageResponseDTO> getMessages(String ticketId, User user) {

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(user==null || ticket==null)
            throw new IllegalArgumentException("User Or Ticker Not found");
        else if(
                !user.getId().equals(ticket.getCreatedBy().getId()) &&
                        (ticket.getAssignedTo() == null ||
                                !user.getId().equals(ticket.getAssignedTo().getId()))
        )
            throw new IllegalArgumentException("This User is not allowed in this chat");

        if(ticket.getStatus() == StatusType.OPEN) {
            ticket.setStatus(StatusType.IN_PROGRESS);
        }

        List<Message> messages = messageRepository.findByTicketOrderByCreatedAtAsc(ticket);

        return messages.stream().map(message -> new MessageResponseDTO(message.getId(), message.getContent(), message.getSender().getId(), message.getCreatedAt())).toList();
    }
}
