package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponseDTO implements Serializable {

    private String id;
    private String title;
    private StatusType status;
    private PriorityType priority;
    private CategoryType category;
    private String createdById;
    private String createdByName;
    private String createdByEmail;
    private String assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}