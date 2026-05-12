package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public User updateUserRole(String userId, RoleType roleType) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(roleType);

        return userRepository.save(user);
    }
}
