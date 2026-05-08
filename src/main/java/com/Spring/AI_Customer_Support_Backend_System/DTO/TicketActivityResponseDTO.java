package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketActivityResponseDTO implements Serializable {

    private ActionType action;
    private String performedById;
    private String performedByName;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
}
