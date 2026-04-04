package com.Spring.AI_Customer_Support_Backend_System.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIResponse {

    private String userMessage;
    private String aiResponse;
    private LocalDateTime timestamp;
}
