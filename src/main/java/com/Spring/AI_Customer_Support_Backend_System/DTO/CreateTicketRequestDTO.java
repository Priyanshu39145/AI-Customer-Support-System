package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketRequestDTO {

    private String title;

    private String description;

    private PriorityType priority;

}
