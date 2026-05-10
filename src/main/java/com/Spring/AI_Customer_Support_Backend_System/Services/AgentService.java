package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AgentCategoriesResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.AgentResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.AssignAgentCategoriesRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tickets", allEntries = true),
            @CacheEvict(value = "searchticketsList", allEntries = true)
    })
    public AgentCategoriesResponseDTO assignCategories(String agentId, AssignAgentCategoriesRequestDTO requestDTO) {
        log.info("Assigning categories to agent {}", agentId);

        if(requestDTO == null || requestDTO.getCategories() == null || requestDTO.getCategories().isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }

        Set<CategoryType> categories = new HashSet<>(requestDTO.getCategories());
        if(categories.contains(null)) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> {
                    log.error("Agent not found: {}", agentId);
                    return new IllegalArgumentException("Agent Not Found");
                });

        if(agent.getRole() != RoleType.AGENT) {
            log.warn("User {} is not an agent", agentId);
            throw new IllegalArgumentException("User is not an Agent");
        }

        agent.setExpertise(categories);
        userRepository.save(agent);

        log.info("Categories {} assigned successfully to agent {}", categories, agentId);
        return new AgentCategoriesResponseDTO(
                agent.getId(),
                agent.getName(),
                agent.getEmail(),
                agent.getExpertise()
        );
    }

    public List<AgentResponseDTO> getAgents() {
        List<StatusType> activeStatuses = List.of(StatusType.OPEN, StatusType.IN_PROGRESS);
        log.info("Fetching agents for admin assignment UI");

        return userRepository.findByRole(RoleType.AGENT)
                .stream()
                .map(agent -> new AgentResponseDTO(
                        agent.getId(),
                        agent.getName(),
                        agent.getEmail(),
                        agent.getExpertise(),
                        ticketRepository.countByAssignedToAndStatusIn(agent, activeStatuses)
                ))
                .collect(Collectors.toList());
    }
}
