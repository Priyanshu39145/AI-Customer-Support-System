package com.Spring.AI_Customer_Support_Backend_System.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email should not be blank")
    private String email;
    @NotBlank(message = "Password should not be blank")
    private String password;
}
