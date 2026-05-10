package com.Spring.AI_Customer_Support_Backend_System.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long openTickets;
    private long resolvedTickets;
    private long highPriorityTickets;
    private long totalTickets;
    private long ticketsAssignedToMe;
}
