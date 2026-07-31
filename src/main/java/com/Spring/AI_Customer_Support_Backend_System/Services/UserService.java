package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AgentResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    //We just change the user Role here ---- first we check if the User is found or not --- then change it ----
    @Transactional
    public User updateUserRole(String userId, RoleType roleType) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(roleType);

        return userRepository.save(user);
    }

    public List<AgentResponseDTO> getAllAgents() {

        List<User> agents = userRepository.findByRole(RoleType.AGENT);

        return agents.stream()
                .map(agent -> new AgentResponseDTO(
                        agent.getId(),
                        agent.getName(),
                        agent.getEmail(),
                        agent.getExpertise(),
                        ticketRepository.countByAssignedToAndStatusIn(
                                agent,
                                List.of(StatusType.OPEN, StatusType.IN_PROGRESS)
                        )
                ))
                .toList();
    }
}
