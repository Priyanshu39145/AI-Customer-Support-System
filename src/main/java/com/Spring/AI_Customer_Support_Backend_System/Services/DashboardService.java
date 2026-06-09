package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.DashboardStatsDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardStatsDTO getStats(User user) {

        if(user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        log.info(
                "Fetching dashboard stats | userId: {}, role: {}",
                user.getId(),
                user.getRole()
        );

        //Then we fetch total tickets, total open tickets, total in progress tickets, total closed tickets
        //From the frontend ---- other things are calculated and put on ----
        if(user.getRole() == RoleType.ADMIN) {

            return DashboardStatsDTO.builder()
                    .totalTickets(ticketRepository.count())
                    .openTickets(ticketRepository.countByStatus(StatusType.OPEN))
                    .inProgressTickets(ticketRepository.countByStatus(StatusType.IN_PROGRESS))

                    .closedTickets(ticketRepository.countByStatus(StatusType.CLOSED))
                    .totalConversations(0)
                    .activeConversations(0)
                    .build();
        }

        if(user.getRole() == RoleType.AGENT) {

            return DashboardStatsDTO.builder()
                    .totalTickets(ticketRepository.countByAssignedTo(user))
                    .openTickets(ticketRepository.countByAssignedToAndStatus(user, StatusType.OPEN))
                    .inProgressTickets(ticketRepository.countByAssignedToAndStatus(user, StatusType.IN_PROGRESS))

                    .closedTickets(ticketRepository.countByAssignedToAndStatus(user, StatusType.CLOSED))
                    .totalConversations(0)
                    .activeConversations(0)
                    .build();
        }

        return DashboardStatsDTO.builder()
                .totalTickets(ticketRepository.countByCreatedBy(user))
                .openTickets(ticketRepository.countByCreatedByAndStatus(user, StatusType.OPEN))
                .inProgressTickets(ticketRepository.countByCreatedByAndStatus(user, StatusType.IN_PROGRESS))

                .closedTickets(ticketRepository.countByCreatedByAndStatus(user, StatusType.CLOSED))
                .totalConversations(0)
                .activeConversations(0)
                .build();
    }
}
