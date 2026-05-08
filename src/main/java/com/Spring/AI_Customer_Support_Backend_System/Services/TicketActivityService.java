package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketActivityResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.TicketActivity;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ActionType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketActivityService {

    private final TicketActivityRepository ticketActivityRepository;

    @Transactional
    public void logActivity(Ticket ticket, User user, ActionType actionType, String oldValue, String newValue) {
        if (ticket == null || user == null || actionType == null) {
            log.warn("Skipping ticket activity log because required data is missing");
            return;
        }

        TicketActivity activity = TicketActivity.builder()
                .ticket(ticket)
                .performedBy(user)
                .actionType(actionType)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        ticketActivityRepository.save(activity);
        log.info("Logged ticket activity {} for ticketId: {} by userId: {}", actionType, ticket.getId(), user.getId());
    }

    @Cacheable(
            value = "ticketHistory",
            key = "#ticketId",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<TicketActivityResponseDTO> getActivitiesForTicket(String ticketId) {
        return ticketActivityRepository.findByTicketIdWithPerformedByOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TicketActivityResponseDTO mapToResponse(TicketActivity activity) {
        User performedBy = activity.getPerformedBy();

        return new TicketActivityResponseDTO(
                activity.getActionType(),
                performedBy != null ? performedBy.getId() : null,
                performedBy != null ? performedBy.getName() : null,
                activity.getOldValue(),
                activity.getNewValue(),
                activity.getCreatedAt()
        );
    }
}
