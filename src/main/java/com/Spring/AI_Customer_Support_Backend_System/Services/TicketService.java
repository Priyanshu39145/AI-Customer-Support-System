package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.*;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Message;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.MessageRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final MessageRepository messageRepository;
    private final ModelMapper modelMapper;

    public CreateTicketResponseDTO createTicket(String userId, CreateTicketRequestDTO requestDTO)    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Ticket ticket = Ticket.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .status(StatusType.OPEN)
                .priority(requestDTO.getPriority() != null ? requestDTO.getPriority() : PriorityType.MEDIUM)
                .createdBy(user)
                .build();
        ticketRepository.save(ticket);

        return modelMapper.map(ticket , CreateTicketResponseDTO.class);
    }


    public Page<TicketResponseDTO> getTicketByStatusAndPriority(StatusType status, PriorityType priority, int page, int size) {
        Page<Ticket> tickets;
        Pageable pageable = PageRequest.of(page,size);
        if(status==null && priority==null)
            tickets = ticketRepository.findAll(pageable);
        else if (status==null) {
            tickets = ticketRepository.findByPriority(priority,pageable);
        }
        else if (priority==null)    {
            tickets = ticketRepository.findByStatus(status,pageable);
        }
        else
            tickets = ticketRepository.findByStatusAndPriority(status,priority,pageable);

        return tickets.map(ticket -> {
            TicketResponseDTO dto = modelMapper.map(ticket, TicketResponseDTO.class);
            dto.setCreatedById(
                    ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null
            );
            dto.setAssignedToId(
                    ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null
            );
            return dto;
        });
    }

    @Transactional
    public TicketResponseDTO assignTicket(String ticketId, String agentId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Ticket Not found"));
        User agent = userRepository.findById(agentId).orElseThrow(() -> new IllegalArgumentException("Agent Not Found"));

        if(agent.getRole()!= RoleType.AGENT)
            throw new IllegalArgumentException("User is not an Agent");

        ticket.setAssignedTo(agent);

        ticket.setStatus(StatusType.IN_PROGRESS);

        TicketResponseDTO dto = modelMapper.map(ticket, TicketResponseDTO.class);
        dto.setCreatedById(
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null
        );
        dto.setAssignedToId(
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null
        );
        return dto;
    }


    @Transactional
    public TicketResponseDTO changeStatus(String ticketId, StatusType status) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Ticket Not found"));

        if(ticket.getAssignedTo() == null) {
            throw new IllegalArgumentException("Ticket is not assigned to any agent");
        }

        StatusType currentStatus = ticket.getStatus();

        if(currentStatus == StatusType.OPEN && status != StatusType.IN_PROGRESS) {
            throw new IllegalArgumentException("Invalid status transition");
        }

        if(currentStatus == StatusType.IN_PROGRESS && status != StatusType.CLOSED) {
            throw new IllegalArgumentException("Invalid status transition");
        }

        if(currentStatus == StatusType.CLOSED) {
            throw new IllegalArgumentException("Ticket is already resolved");
        }

        ticket.setStatus(status);

        TicketResponseDTO dto = modelMapper.map(ticket, TicketResponseDTO.class);
        dto.setCreatedById(
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null
        );
        dto.setAssignedToId(
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null
        );
        return dto;

    }

    public TicketDetailedResponseDTO getTicketById(String ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket==null || user==null)
            throw new IllegalArgumentException("User or Ticket doesnt exist");
        else if(
                !user.getId().equals(ticket.getCreatedBy().getId()) &&
                        (ticket.getAssignedTo() == null ||
                                !user.getId().equals(ticket.getAssignedTo().getId()))
        )
            throw new IllegalArgumentException("This User is not allowed in this chat");

        //We are fetching messages directly from ticket --- Lazy Fetch applied ---
        //We are fetching messages direct by message repository ---
        List<Message> messages = messageRepository.findByTicketOrderByCreatedAtAsc(ticket);

        List<MessageResponseDTO> messageDTOs = messages.stream().map(message -> new MessageResponseDTO(message.getId(), message.getContent(), message.getSender().getId(), message.getCreatedAt())).toList();

        return new
                TicketDetailedResponseDTO(ticketId,ticket.getTitle(),ticket.getDescription(),ticket.getStatus(),ticket.getPriority(),ticket.getCreatedBy().getId(),ticket.getAssignedTo().getId(),messageDTOs);

    }

    public Page<TicketResponseDTO> getTicketsOfUser(User user, StatusType status, PriorityType priority, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);

        Page<Ticket> tickets;

        if(status==null && priority==null)
            tickets = ticketRepository.findByCreatedBy(user,pageable);
        else if(status==null)
            tickets = ticketRepository.findByCreatedByAndPriority(user,priority,pageable);
        else if(priority==null)
            tickets = ticketRepository.findByCreatedByAndStatus(user,status,pageable);
        else
            tickets = ticketRepository.findByCreatedByAndStatusAndPriority(user,status,priority,pageable);

        Page<TicketResponseDTO> ticketDTOs = tickets.map(ticket -> new TicketResponseDTO(ticket.getId(), ticket.getTitle(), ticket.getStatus(),ticket.getPriority(),ticket.getCreatedBy().getId(),ticket.getAssignedTo().getId()));
        return ticketDTOs;
    }

    public Page<TicketResponseDTO> getTicketsOfAgent(User user, StatusType status, PriorityType priority, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);

        Page<Ticket> tickets;

        if(status==null && priority==null)
            tickets = ticketRepository.findByAssignedTo(user,pageable);
        else if(status==null)
            tickets = ticketRepository.findByAssignedToAndPriority(user,priority,pageable);
        else if(priority==null)
            tickets = ticketRepository.findByAssignedToAndStatus(user,status,pageable);
        else
            tickets = ticketRepository.findByAssignedToAndStatusAndPriority(user,status,priority,pageable);

        Page<TicketResponseDTO> ticketDTOs = tickets.map(ticket -> new TicketResponseDTO(ticket.getId(), ticket.getTitle(), ticket.getStatus(),ticket.getPriority(),ticket.getCreatedBy().getId(),ticket.getAssignedTo().getId()));
        return ticketDTOs;
    }
}
