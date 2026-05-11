package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private UserDTO user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserDTO {
        private String id;
        private String email;
        private String name;
        private RoleType role;
    }

    public LoginResponseDTO(String email, RoleType role, String jwtToken) {
        this.accessToken = jwtToken;
        this.refreshToken = null;
    }
}
