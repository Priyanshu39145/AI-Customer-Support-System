package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketRequestDTO {
    @NotBlank(message = "title is required")
    private String title;
    @NotBlank(message = "description is required")
    private String description;

    private PriorityType priority;

}
