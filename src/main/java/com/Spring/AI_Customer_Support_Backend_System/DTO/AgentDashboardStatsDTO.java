package com.Spring.AI_Customer_Support_Backend_System.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentDashboardStatsDTO {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
}
//Done