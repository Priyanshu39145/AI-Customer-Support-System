package com.Spring.AI_Customer_Support_Backend_System.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email should not be blank")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
}
//This carries the user credentials supplied by the frontend --- validation is provided ----
