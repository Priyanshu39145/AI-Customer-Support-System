package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    private String email;
    private RoleType role;
    private String jwtToken;
    private String accessToken;
    private String refreshToken;

    public LoginResponseDTO(String email, RoleType role, String jwtToken) {
        this.email = email;
        this.role = role;
        this.jwtToken = jwtToken;
        this.accessToken = jwtToken;
    }
}
