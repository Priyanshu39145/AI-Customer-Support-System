package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.*;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ActionType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
//    private final MessageRepository messageRepository;
    private final ModelMapper modelMapper;

    private final EmailServices emailServices;

    private final TicketActivityService ticketActivityService;


//
    //This will update the values in all existing cache keys --
    //Here we create a ticket using the given user and a create request d uh create ticket request DTO
// which consists title and description. These are the required required fields and also a conversation.
// Conversation will remain null for the user manually creating a ticket.
    @Caching(evict = {
            @CacheEvict(value = "searchticketsList", allEntries = true),
            @CacheEvict(value = "tickets", allEntries = true)
    })
    @Transactional
    public CreateTicketResponseDTO createTicket(User user, CreateTicketRequestDTO requestDTO, Conversation conversation, PriorityType priority, CategoryType category)    {
        log.info("Creating ticket for userId: {}", user.getId());
        if(conversation != null && conversation.getTicket() != null) {
            throw new IllegalArgumentException("Conversation already has a ticket");
        }

        PriorityType resolvedPriority = priority != null ? priority : PriorityType.MEDIUM;
        CategoryType resolvedCategory = category != null ? category : CategoryType.GENERAL;

        // Find agent - returns null if no agents available
        User agent = findLeastLoadedAgentByCategory(resolvedCategory);
        if (agent == null) {
            log.warn("No agents available for category {} - ticket will be created without assignment", resolvedCategory);
        }

        Ticket ticket = Ticket.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .status(StatusType.OPEN)
                .priority(resolvedPriority)
                .category(resolvedCategory)
                .conversation(conversation)
                .createdBy(user)
                .assignedTo(agent)
                .build();
        ticketRepository.save(ticket);
        if(conversation != null) {
            conversation.setTicket(ticket);
        }
        //We log in ticketActivity ----
        ticketActivityService.logActivity(ticket, user, ActionType.CREATED, null, ticket.getId());
        //If agent is also assigned then we also log that in ticketActivity ----
        if(agent != null) {
            ticketActivityService.logActivity(ticket, user, ActionType.ASSIGNED, null, formatUserActivityValue(agent));
        }
        log.info("Ticket created successfully with id: {}", ticket.getId());
        return modelMapper.map(ticket , CreateTicketResponseDTO.class);
    }


    //Similarly Cache Evict for updating the Cache
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tickets", allEntries = true),
            @CacheEvict(value = "searchticketsList", allEntries = true),
            @CacheEvict(value = "ticketComments", allEntries = true),
            @CacheEvict(value = "ticketHistory", key = "#ticketId")
    })
    public TicketResponseDTO assignTicket(User user, String ticketId, String agentId) {
        //Validating the given things ---
        log.info("Assigning ticket {} to agent {}", ticketId, agentId);
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->{
            log.error("Ticket not found: {}", ticketId);
            return new IllegalArgumentException("Ticket Not found");});

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> {
                    log.error("Agent not found: {}", agentId);
                    return new IllegalArgumentException("Agent Not Found");
                });
        //If the selected user by the admin --- is not an agent we return an exception ----
        if(agent.getRole()!= RoleType.AGENT) {
            log.warn("User {} is not an agent", agentId);
            throw new IllegalArgumentException("User is not an Agent");
        }
        //We log in the ticketActivityService and return the DTO ----
        User previousAgent = ticket.getAssignedTo();
        ticket.setAssignedTo(agent);
        ticketRepository.save(ticket);
        ticketActivityService.logActivity(
                ticket,
                user,
                ActionType.ASSIGNED,
                formatUserActivityValue(previousAgent),
                formatUserActivityValue(agent)
        );

        log.info("Ticket {} assigned successfully to agent {}", ticketId, agentId);

        TicketResponseDTO dto = modelMapper.map(ticket, TicketResponseDTO.class);
        dto.setCreatedById(
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null
        );
        dto.setAssignedToId(
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null
        );
        return dto;
    }


    //This is the changeStatus service implementation ---
    //First of all we are doing cacheEvict so that the ticket details get updated inside th cache too ----
    @Caching(evict = {
            @CacheEvict(value = "searchticketsList", allEntries = true),
            @CacheEvict(value = "tickets", allEntries = true),
            @CacheEvict(value = "ticketHistory", key = "#ticketId")
    })
    @Transactional
    public TicketResponseDTO changeStatus(User user, String ticketId, StatusType status) {
        log.info("Changing status of ticket {} to {}", ticketId, status);
        //We validate the given details ----
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketId);
                    return new IllegalArgumentException("Ticket Not found");
                });
        //If the ticker has no assigned agent or the assigned agent doesnt match to the current agent USer --- then we throw error
        if(ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(user.getId())) {
            log.warn("Ticket {} has no assigned agent", ticketId);
            throw new AccessDeniedException("Ticket is not assigned to this agent");
        }

        StatusType currentStatus = ticket.getStatus();
        //We can only go from Open to In Progress and from In Progress to Closed ----
        if(currentStatus == StatusType.OPEN && status != StatusType.IN_PROGRESS) {
            throw new IllegalArgumentException("Invalid status transition");
        }

        if(currentStatus == StatusType.IN_PROGRESS && status != StatusType.CLOSED) {
            throw new IllegalArgumentException("Invalid status transition");
        }
        //If the ticket is already closed then we cant change its status
        if(currentStatus == StatusType.CLOSED) {
            throw new IllegalArgumentException("Ticket is already resolved");
        }
        //We set the status ---- and set the updated time ----
        //And also we log the activity inside the ticketActivityService ----
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        ticketActivityService.logActivity(
                ticket,
                user,
                ActionType.STATUS_CHANGED,
                currentStatus != null ? currentStatus.name() : null,
                status.name()
        );

        log.info("Ticket {} status updated successfully", ticketId);

        //Now if the ticket is closed we send an email to the user of the ticker notifying that the ticket is closed ----
        if(ticket.getStatus()==StatusType.CLOSED)
            emailServices.sendEmail(ticket);

        //We create the DTO and then return it ----
        TicketResponseDTO dto = modelMapper.map(ticket, TicketResponseDTO.class);
        dto.setCreatedById(
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null
        );
        dto.setAssignedToId(
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null
        );
        return dto;

    }
    //Similarly we here --- use Cache Evict so that the tickets inside the cache also get changed accordingly ---
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "searchticketsList", allEntries = true),
            @CacheEvict(value = "tickets", allEntries = true)

    })
    public TicketResponseDTO changePriority(User user, String ticketId, PriorityType priority) {
        log.info("Changing priority of ticket {} to {}", ticketId, priority);
        //We validate the details given ----
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketId);
                    return new IllegalArgumentException("Ticket Not found");
                });
        //This is a private method created that checks whether the user has the role to change the particular ticket
        validateAgentOrAdminCanMutateTicket(user, ticket);

        //After all validations ----
        //We first change the priority ---
        //Then set the updated Time ---
        //Then we update the ticketActivityStatus ---- logging that the priority was being changed ----
        PriorityType oldPriority = ticket.getPriority();
        ticket.setPriority(priority);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        ticketActivityService.logActivity(
                ticket,
                user,
                ActionType.PRIORITY_CHANGED,
                oldPriority != null ? oldPriority.name() : null,
                priority.name()
        );
        //Then we jsut create the DTO and send it ----
        log.info("Ticket {} priority updated successfully", ticketId);

        TicketResponseDTO dto = modelMapper.map(ticket, TicketResponseDTO.class);
        dto.setCreatedById(
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null
        );
        dto.setAssignedToId(
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null
        );
        return dto;
    }

    //Similarly we here --- use Cache Evict so that the tickets inside the cache also get changed accordingly ---
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "searchticketsList", allEntries = true),
            @CacheEvict(value = "tickets", allEntries = true),
            @CacheEvict(value = "ticketHistory", key = "#ticketId")
    })
    public TicketResponseDTO changeCategory(User user, String ticketId, CategoryType category) {
        log.info("Changing category of ticket {} to {}", ticketId, category);
        //Validating user, category, ticket and the role
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketId);
                    return new IllegalArgumentException("Ticket Not found");
                });

        validateAgentOrAdminCanMutateTicket(user, ticket);
        //We change the category ---
        CategoryType oldCategory = ticket.getCategory();
        User previousAgent = ticket.getAssignedTo();
        ticket.setCategory(category);
        //And if the ticket is still open --- we again change the agent as the agent is decided on the basis of category ---
        if(ticket.getStatus()==StatusType.OPEN) {
            //See the method --- there we find the least loaded agent by category ----
            User newAgent = findLeastLoadedAgentByCategory(category);
            //If the previous agent was null or the new agent is not equal to the previous agent  ---- then we need to assign the agent
            if (!Objects.equals(
                    previousAgent != null ? previousAgent.getId() : null,
                    newAgent != null ? newAgent.getId() : null
            )) {
                ticket.setAssignedTo(newAgent);
            }
        }
        //We make the changes and then again change the ticketActivityService ----
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        ticketActivityService.logActivity(
                ticket,
                user,
                ActionType.CATEGORY_CHANGED,
                oldCategory != null ? oldCategory.name() : null,
                category.name()
        );
        //If the new and previous agent are not equal then we also need to log it into the ticketActivityService that the agent is new assigned
        //formatUserActivityValue ---- returns agentId-agentName
        User currentAgent = ticket.getAssignedTo();
        if (!Objects.equals(
                previousAgent != null ? previousAgent.getId() : null,
                currentAgent != null ? currentAgent.getId() : null
        )) {
            ticketActivityService.logActivity(
                    ticket,
                    user,
                    ActionType.ASSIGNED,
                    formatUserActivityValue(previousAgent),
                    formatUserActivityValue(currentAgent)
            );
        }

        log.info("Ticket {} category updated successfully", ticketId);
        return mapToTicketResponseDTO(ticket);
    }

    //Cached method ---
    //We just validate everything ---
    //And send the ResponseDT) for that particular ticket ----
    @Cacheable(value = "tickets", key = "#ticketId + '-' + #user.id")
    public TicketDetailedResponseDTO getTicketById(String ticketId, User user) {

        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        log.info("Fetching ticket details for ticketId: {} by user: {}", ticketId, user.getId());

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (user.getRole() != RoleType.ADMIN &&
                !user.getId().equals(ticket.getCreatedBy().getId()) &&
                (ticket.getAssignedTo() == null ||
                        !user.getId().equals(ticket.getAssignedTo().getId()))) {

            log.warn("Unauthorized access to ticket {} by user {}", ticketId, user.getId());
            throw new AccessDeniedException("Not allowed");
        }

        String conversationId = ticket.getConversation() != null
                ? ticket.getConversation().getId()
                : null;

        String assignedToId = ticket.getAssignedTo() != null
                ? ticket.getAssignedTo().getId()
                : null;
        String assignedToName = ticket.getAssignedTo() != null
                ? ticket.getAssignedTo().getName()
                : null;
        String assignedToEmail = ticket.getAssignedTo() != null
                ? ticket.getAssignedTo().getEmail()
                : null;
        User createdBy = ticket.getCreatedBy();

        return new TicketDetailedResponseDTO(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                conversationId,
                createdBy.getId(),
                createdBy.getName(),
                createdBy.getEmail(),
                assignedToId,
                assignedToName,
                assignedToEmail,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    //Here we fetch the ticketHistory fro a particular ticket --- and show in the detailed response of the ticket ---
    public List<TicketActivityResponseDTO> getTicketHistory(String ticketId, User user) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        log.info("Fetching ticket history for ticketId: {} by user: {}", ticketId, user.getId());

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        validateTicketParticipant(ticket, user);
        return ticketActivityService.getActivitiesForTicket(ticketId);
    }
//
    //This is the search Tickets endpoint ---
    //HEre we search the tickets on the basis of many filters --- keyword,status,priority, assignedToId, created dates
    //We have cached it ---- with suitable key ---- the cache is evicted in the createTicket function ---
    //unless is used for empty pages of content ---- then cache is not kept ----
    @Cacheable(
            value = "searchticketsList",
            key = "#user.id + '-' + #user.role + '-' + (#keyword ?: 'ALL') + '-' + (#status ?: 'ALL') + '-' + (#priority ?: 'ALL') + '-' + (#category ?: 'ALL') + '-' + (#assignedToId ?: 'ALL') + '-' + (#createdFrom ?: 'NA') + '-' + (#createdTo ?: 'NA') + '-' + #page + '-' + #size",
            unless = "#result == null || #result.content.isEmpty()"
    )
    public Page<TicketResponseDTO> getTicketsOfUser(User user,
                                                    String keyword,
                                                    StatusType status,
                                                    PriorityType priority,
                                                    CategoryType category,
                                                    String assignedToId,
                                                    LocalDate createdFrom,
                                                    LocalDate createdTo,
                                                    int page,
                                                    int size) {

        if (user == null) {
            throw new IllegalArgumentException("Unauthorized");
        }

        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        log.info("Searching tickets belonging to userId: {}", user.getId());
        Pageable pageable = PageRequest.of(page,size);

        LocalDateTime createdFromDateTime = createdFrom != null ? createdFrom.atStartOfDay() : null;
        LocalDateTime createdToDateTime = createdTo != null ? createdTo.plusDays(1).atStartOfDay().minusNanos(1) : null;
        
        Page<Ticket> tickets = null;
        //IMP ---- if the role is user --- we send the tickets where createdBy is user
        //If the role is of AGENT ---- we send the tickets where assignedTo is user
        //The ticket contains short info ----
        if(user.getRole()==RoleType.USER)   {
            tickets = ticketRepository.searchTicketsBelongingToUser(
                    user,
                    keyword,
                    status,
                    priority,
                    category,
                    assignedToId,
                    createdFromDateTime,
                    createdToDateTime,
                    pageable
            );
        }
        else if(user.getRole()==RoleType.AGENT) {
            tickets = ticketRepository.searchTicketsBelongingToAgent(
                    user,
                    keyword,
                    status,
                    priority,
                    category,
                    assignedToId,
                    createdFromDateTime,
                    createdToDateTime,
                    pageable
            );
        }

        return tickets.map(this::mapToTicketResponseDTO);
    }

    //It is a cached method ----
    //We just validate the access and send the paginated output ----
    @Cacheable(
            value = "searchticketsList",
            key = "'ADMIN-ALL-' + (#keyword ?: 'ALL') + '-' + (#status ?: 'ALL') + '-' + (#priority ?: 'ALL') + '-' + (#category ?: 'ALL') + '-' + (#assignedToId ?: 'ALL') + '-' + (#createdFrom ?: 'NA') + '-' + (#createdTo ?: 'NA') + '-' + #page + '-' + #size",
            unless = "#result == null || #result.content.isEmpty()"
    )
    public Page<TicketResponseDTO> getAllTicketsForAdmin(User user,
                                                         String keyword,
                                                         StatusType status,
                                                         PriorityType priority,
                                                         CategoryType category,
                                                         String assignedToId,
                                                         LocalDate createdFrom,
                                                         LocalDate createdTo,
                                                         int page,
                                                         int size) {

        if (user == null || user.getRole() != RoleType.ADMIN) {
            throw new AccessDeniedException("Admin access required");
        }

        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime createdFromDateTime = createdFrom != null ? createdFrom.atStartOfDay() : null;
        LocalDateTime createdToDateTime = createdTo != null ? createdTo.plusDays(1).atStartOfDay().minusNanos(1) : null;

        return ticketRepository.searchAllTickets(
                keyword,
                status,
                priority,
                category,
                assignedToId,
                createdFromDateTime,
                createdToDateTime,
                pageable
        ).map(this::mapToTicketResponseDTO);
    }

    //Here we find the agent with the least amount of tickets by category ----
    public User findLeastLoadedAgentByCategory(CategoryType category) {
        //We make an arraylist of the statuses --- applicable for counting tickets
        List<StatusType> activeStatuses = List.of(StatusType.OPEN, StatusType.IN_PROGRESS);
        //We set the Pageable instance so that we get the first agent only ---
        Pageable firstAgent = PageRequest.of(0, 1);
        if(category != null) {
            //Now what do we find using our JPA Query method? ----
            //We return the agent with the least amount of open or in progress tickets that
            List<User> matchingAgents = userRepository.findLeastLoadedAgentsByCategory(
                    RoleType.AGENT,
                    category,
                    activeStatuses,
                    firstAgent
            );

            if(!matchingAgents.isEmpty()) {
                log.info("Assigned ticket category {} to matching agent {}", category, matchingAgents.get(0).getId());
                return matchingAgents.get(0);
            }
            //If there are no matching agents --- then we find just the least Loaded Agent ---- and return it
            log.warn("No agent found with expertise {}. Falling back to least-loaded agent.", category);
        }

        return userRepository.findLeastLoadedAgents(
                RoleType.AGENT,
                activeStatuses,
                firstAgent
        ).stream().findFirst().orElse(null);
    }


    //If it is Admin --- then it has privileges ---
    //If it is Agent --- we check whether the ticket is assigned to it ----
    private void validateAgentOrAdminCanMutateTicket(User user, Ticket ticket) {
        if(user.getRole() == RoleType.ADMIN) {
            return;
        }

        if(user.getRole() == RoleType.AGENT &&
                ticket.getAssignedTo() != null &&
                ticket.getAssignedTo().getId().equals(user.getId())) {
            return;
        }

        log.warn("User {} with role {} is not allowed to update ticket {}", user.getId(), user.getRole(), ticket.getId());
        throw new AccessDeniedException("Not allowed to update this ticket");
    }

    private void validateTicketParticipant(Ticket ticket, User user) {
        if (user.getRole() == RoleType.ADMIN) {
            return;
        }

        boolean isCreator = ticket.getCreatedBy() != null && user.getId().equals(ticket.getCreatedBy().getId());
        boolean isAssignedAgent = ticket.getAssignedTo() != null && user.getId().equals(ticket.getAssignedTo().getId());

        if (!isCreator && !isAssignedAgent) {
            log.warn("Unauthorized ticket history access | ticketId: {}, userId: {}", ticket.getId(), user.getId());
            throw new AccessDeniedException("Not allowed to access history for this ticket");
        }
    }

    private String formatUserActivityValue(User user) {
        if(user == null) {
            return null;
        }

        return user.getId() + " - " + user.getName();
    }

    private TicketResponseDTO mapToTicketResponseDTO(Ticket ticket) {
        User createdBy = ticket.getCreatedBy();
        User assignedTo = ticket.getAssignedTo();

        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                createdBy != null ? createdBy.getId() : null,
                createdBy != null ? createdBy.getName() : null,
                createdBy != null ? createdBy.getEmail() : null,
                assignedTo != null ? assignedTo.getId() : null,
                assignedTo != null ? assignedTo.getName() : null,
                assignedTo != null ? assignedTo.getEmail() : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }


}
