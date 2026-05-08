package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDetailedResponseDTO implements Serializable {
    private String ticketId;
    private String title;
    private String description;
    private StatusType status;
    private PriorityType priority;
    private CategoryType category;
    private String conversationId;
    private String createdById;
    private String assignedToId;

}
