package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketCommentRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketCommentResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.TicketComment;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ActionType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketCommentRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCommentService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketActivityService ticketActivityService;
    //Cached method ----
    //We find all the ticket comments for a particular ticket ---- with a particular user ---- Agent or User
    @Cacheable(
            value = "ticketComments",
            key = "#ticketId + '-' + (#user != null ? #user.id : 'anonymous')",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<TicketCommentResponseDTO> getCommentsForTicket(String ticketId, User user) {
        log.info("Fetching comments for ticketId: {} by userId: {}", ticketId, user != null ? user.getId() : null);
        //We get the ticket --- validate the user
        Ticket ticket = getTicketOrThrow(ticketId);
        validateTicketParticipant(ticket, user);
        //Then we find the ticket comment with its author info --- in ascending order of createdAt
        return ticketCommentRepository.findByTicketIdWithAuthorOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Caching(evict = {
            @CacheEvict(
                    value = "ticketComments",
                    key = "#ticketId + '-' + (#user != null ? #user.id : 'anonymous')"
            ),
            @CacheEvict(value = "ticketHistory", key = "#ticketId")
    })
    @Transactional
    public TicketCommentResponseDTO addComment(String ticketId, User user, TicketCommentRequestDTO requestDTO) {
        log.info("Adding comment for ticketId: {} by userId: {}", ticketId, user != null ? user.getId() : null);
        //We get the ticket --- and its participant validated ----
        Ticket ticket = getTicketOrThrow(ticketId);
        validateTicketParticipant(ticket, user);

        TicketComment comment = TicketComment.builder()
                .content(requestDTO.getContent().trim())
                .ticket(ticket)
                .author(user)
                .authorRole(user.getRole())
                .build();

        //We create the entity --- and save it --- and map the response and send it
        TicketComment savedComment = ticketCommentRepository.save(comment);
        ticketActivityService.logActivity(
                ticket,
                user,
                ActionType.COMMENT_ADDED,
                null,
                savedComment.getContent()
        );
        log.info("Comment created successfully with id: {}", savedComment.getId());

        return mapToResponse(savedComment);
    }

    private Ticket getTicketOrThrow(String ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    private void validateTicketParticipant(Ticket ticket, User user) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (user.getRole() == RoleType.ADMIN) {
            return;
        }

        boolean isCreator = ticket.getCreatedBy() != null && user.getId().equals(ticket.getCreatedBy().getId());
        boolean isAssignedAgent = ticket.getAssignedTo() != null && user.getId().equals(ticket.getAssignedTo().getId());

        if (!isCreator && !isAssignedAgent) {
            log.warn("Unauthorized ticket comment access | ticketId: {}, userId: {}", ticket.getId(), user.getId());
            throw new AccessDeniedException("Not allowed to access comments for this ticket");
        }
    }

    private TicketCommentResponseDTO mapToResponse(TicketComment comment) {
        User author = comment.getAuthor();

        return new TicketCommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getTicket().getId(),
                author != null ? author.getId() : null,
                author != null ? author.getName() : null,
                comment.getAuthorRole(),
                comment.getCreatedAt()
        );
    }
}
