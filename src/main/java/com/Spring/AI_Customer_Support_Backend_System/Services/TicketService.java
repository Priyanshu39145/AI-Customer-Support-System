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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final MessageRepository messageRepository;
    private final ModelMapper modelMapper;

    //This will update the values in all existing cache keys --
    @Caching(evict = {
            @CacheEvict(value = "tickets", key = "#ticketId + '-' + #user.id"),
            @CacheEvict(value = "ticketsList", allEntries = true),
            @CacheEvict(value = "ticketsListOfUser", allEntries = true),
            @CacheEvict(value = "ticketsListOfAgent", allEntries = true),
            @CacheEvict(value = "searchticketsList", allEntries = true)
    })
    public CreateTicketResponseDTO createTicket(String userId, CreateTicketRequestDTO requestDTO)    {
        log.info("Creating ticket for userId: {}", userId);
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
        log.info("Ticket created successfully with id: {}", ticket.getId());
        return modelMapper.map(ticket , CreateTicketResponseDTO.class);
    }


    @Cacheable(value = "ticketsList", key = "#page + '-' + #size + '-' + #status + '-' + #priority")//Here we cache the output of this request using the parameters value as key ----
    public Page<TicketResponseDTO> getTicketByStatusAndPriority(StatusType status, PriorityType priority, int page, int size) {
        log.info("Fetching tickets list | page: {}, size: {}, status: {}, priority: {}", page, size, status, priority);
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

        log.debug("Fetched {} tickets", tickets.getTotalElements());

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
    @Caching(evict = {
            @CacheEvict(value = "tickets", key = "#ticketId + '-' + #agentId"),
            @CacheEvict(value = "ticketsList", allEntries = true),
            @CacheEvict(value = "ticketsListOfUser", allEntries = true),
            @CacheEvict(value = "ticketsListOfAgent", allEntries = true),
            @CacheEvict(value = "searchticketsList", allEntries = true)
    })
    public TicketResponseDTO assignTicket(String ticketId, String agentId) {
        log.info("Assigning ticket {} to agent {}", ticketId, agentId);
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->{
            log.error("Ticket not found: {}", ticketId);
            return new IllegalArgumentException("Ticket Not found");});

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> {
                    log.error("Agent not found: {}", agentId);
                    return new IllegalArgumentException("Agent Not Found");
                });

        if(agent.getRole()!= RoleType.AGENT) {
            log.warn("User {} is not an agent", agentId);
            throw new IllegalArgumentException("User is not an Agent");
        }

        ticket.setAssignedTo(agent);

        ticket.setStatus(StatusType.IN_PROGRESS);

        log.info("Ticket {} assigned successfully", ticketId);

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
    @Caching(evict = {
            @CacheEvict(value = "tickets", key = "#ticketId + '-' + #status"),
            @CacheEvict(value = "ticketsList", allEntries = true),
            @CacheEvict(value = "ticketsListOfUser", allEntries = true),
            @CacheEvict(value = "ticketsListOfAgent", allEntries = true),
            @CacheEvict(value = "searchticketsList", allEntries = true)
    })
    public TicketResponseDTO changeStatus(String ticketId, StatusType status) {
        log.info("Changing status of ticket {} to {}", ticketId, status);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketId);
                    return new IllegalArgumentException("Ticket Not found");
                });

        if(ticket.getAssignedTo() == null) {
            log.warn("Ticket {} has no assigned agent", ticketId);
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

        log.info("Ticket {} status updated successfully", ticketId);

        TicketResponseDTO dto = modelMapper.map(ticket, TicketResponseDTO.class);
        dto.setCreatedById(
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null
        );
        dto.setAssignedToId(
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null
        );
        return dto;

    }

    @Cacheable(value = "tickets", key = "#ticketId + '-' + #user.id") //Here we store the Cache with the ticketId and userId as the key -- Here user.id automatically gets the Id using the user.getId method
    public TicketDetailedResponseDTO getTicketById(String ticketId, User user) {
        log.info("Fetching ticket details for ticketId: {} by user: {}", ticketId, user.getId());
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if(ticket==null || user==null) {
            log.error("Ticket/User not found. ticketId: {}, userId: {}", ticketId, user != null ? user.getId() : null);
            throw new IllegalArgumentException("User or Ticket doesnt exist");
        }
        else if(
                !user.getId().equals(ticket.getCreatedBy().getId()) &&
                        (ticket.getAssignedTo() == null ||
                                !user.getId().equals(ticket.getAssignedTo().getId()))
        ) {
            log.warn("Unauthorized access to ticket {} by user {}", ticketId, user.getId());
            throw new IllegalArgumentException("This User is not allowed in this chat");
        }

        //We are fetching messages directly from ticket --- Lazy Fetch applied ---
        //We are fetching messages direct by message repository ---
        List<Message> messages = messageRepository.findByTicketOrderByCreatedAtAsc(ticket);

        log.debug("Fetched {} messages for ticket {}", messages.size(), ticketId);

        List<MessageResponseDTO> messageDTOs = messages.stream().map(message -> new MessageResponseDTO(message.getId(), message.getContent(), message.getSender().getId(), message.getCreatedAt())).toList();

        return new
                TicketDetailedResponseDTO(ticketId,ticket.getTitle(),ticket.getDescription(),ticket.getStatus(),ticket.getPriority(),ticket.getCreatedBy().getId(),ticket.getAssignedTo().getId(),messageDTOs);

    }

    @Cacheable(value = "ticketsListOfUser", key = "#page + '-' + #size + '-' + #status + '-' + #priority + '-' + #user.id")
    public Page<TicketResponseDTO> getTicketsOfUser(User user, StatusType status, PriorityType priority, int page, int size) {
        log.info("Fetching USER tickets for userId: {}", user.getId());
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
    @Cacheable(value = "ticketsListOfAgent", key = "#page + '-' + #size + '-' + #status + '-' + #priority + '-' + #user.id")
    public Page<TicketResponseDTO> getTicketsOfAgent(User user, StatusType status, PriorityType priority, int page, int size) {
        log.info("Fetching AGENT tickets for agentId: {}", user.getId());
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

    @Cacheable(value = "searchticketsList", key = "#keyword + '-' + #page + '-' + #size + '-' + #status + '-' + #priority + '-' + #user.id")
    public Page<TicketResponseDTO> searchTickets(User user,String keyword, int page, int size, StatusType status, PriorityType priority) {
        log.info("Searching tickets | keyword: {}, userId: {}", keyword, user.getId());
        Pageable pageable = PageRequest.of(page,size);
        if(user==null || user.getRole()==RoleType.USER) {
            log.warn("Unauthorized search attempt by user: {}", user != null ? user.getId() : null);
            throw new IllegalArgumentException("User is not allowed to see");
        }

        if(keyword == null || keyword.isBlank()) {
            // call normal filtering method
            return getTicketByStatusAndPriority(status, priority, page, size);
        }

        Page<Ticket> tickets = null;
        if(status==null && priority==null)
            tickets = ticketRepository.searchTickets(keyword,pageable);
        else if(status==null)
            tickets = ticketRepository.searchTicketsByPriority(keyword,priority,pageable);
        else if(priority==null)
            tickets = ticketRepository.searchTicketsByStatus(keyword,status,pageable);
        else
            tickets = ticketRepository.searchTicketsByStatusAndPriority(keyword,status,priority,pageable);

        Page<TicketResponseDTO> ticketDTOs = tickets.map(ticket -> new TicketResponseDTO(ticket.getId(), ticket.getTitle(), ticket.getStatus(),ticket.getPriority(),ticket.getCreatedBy().getId(),ticket.getAssignedTo().getId()));
        return ticketDTOs;
    }
}
