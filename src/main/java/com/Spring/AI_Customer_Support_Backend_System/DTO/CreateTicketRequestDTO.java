package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketRequestDTO {
    @NotBlank(message = "title is required")
    @Size(max = 150)
    private String title;
    @NotBlank(message = "description is required")
    private String description;
    private CategoryType category;

    public CreateTicketRequestDTO(String title, String description) {
        this.title = title;
        this.description = description;
    }

}
//Done ----