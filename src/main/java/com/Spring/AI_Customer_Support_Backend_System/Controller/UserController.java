package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.UpdateRoleRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.UserProfileDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import com.Spring.AI_Customer_Support_Backend_System.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/users/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        ));
    }


    @PutMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRole(
            @PathVariable String userId,
            @RequestBody UpdateRoleRequestDTO requestDTO
    ) {

        return ResponseEntity.ok(
                userService.updateUserRole(userId, requestDTO.getRole())
        );
    }


    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(userRepository.findAll());
    }


    @GetMapping("/admin/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllAgents() {

        List<User> agents = userRepository.findByRole(RoleType.AGENT);

        return ResponseEntity.ok(agents);
    }
}
