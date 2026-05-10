package com.Spring.AI_Customer_Support_Backend_System.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationTitleRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 80, message = "Title cannot exceed 80 characters")
    private String title;
}
