package com.Spring.AI_Customer_Support_Backend_System.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
//Done
