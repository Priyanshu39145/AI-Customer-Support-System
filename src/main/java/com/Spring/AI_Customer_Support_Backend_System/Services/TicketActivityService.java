package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketActivityResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.TicketActivity;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ActionType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketActivityService {

    private final TicketActivityRepository ticketActivityRepository;

    //In this method --- using the log information --- we create the ticketActivity instance and store in DB ---
    //Whenever a ticket has any change or create we do this --- for showing the user the history of the ticket process ---
    //Cache Evict for updating the Cached OUtput ----
    @CacheEvict(
            value = "ticketHistory",
            key = "#ticketId"
    )
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

    //This is cached ---- by the ticketId key ---
    @Cacheable(
            value = "ticketHistory",
            key = "#ticketId",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true) //readOnly tells that this method will only read data from the database and will not modify it
    public List<TicketActivityResponseDTO> getActivitiesForTicket(String ticketId) {
        //We find the ticketActivity with respect to the ticketId and with the PerformedBy information arranged in ascending order fo time ---
        //We map the response to the TicketActivityResponseDTO ---- and return it
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
