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

        log.info("Fetching dashboard stats | userId: {}, role: {}", user.getId(), user.getRole());

        if(user.getRole() == RoleType.ADMIN) {
            return new DashboardStatsDTO(
                    ticketRepository.countByStatus(StatusType.OPEN),
                    ticketRepository.countByStatus(StatusType.CLOSED),
                    ticketRepository.countByPriority(PriorityType.HIGH),
                    ticketRepository.count(),
                    0
            );
        }

        if(user.getRole() == RoleType.AGENT) {
            long assignedToMe = ticketRepository.countByAssignedTo(user);
            return new DashboardStatsDTO(
                    ticketRepository.countByAssignedToAndStatus(user, StatusType.OPEN),
                    ticketRepository.countByAssignedToAndStatus(user, StatusType.CLOSED),
                    ticketRepository.countByAssignedToAndPriority(user, PriorityType.HIGH),
                    assignedToMe,
                    assignedToMe
            );
        }

        return new DashboardStatsDTO(
                ticketRepository.countByCreatedByAndStatus(user, StatusType.OPEN),
                ticketRepository.countByCreatedByAndStatus(user, StatusType.CLOSED),
                ticketRepository.countByCreatedByAndPriority(user, PriorityType.HIGH),
                ticketRepository.countByCreatedBy(user),
                0
        );
    }
}
