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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.*;
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
            @CacheEvict(value = "searchticketsList", allEntries = true),
            @CacheEvict(value = "agents", allEntries = true)
    })
    public AgentCategoriesResponseDTO assignCategories(String agentId, AssignAgentCategoriesRequestDTO requestDTO) {
        log.info("Assigning categories to agent {}", agentId);
        //We validate the DTOs ---
        if(requestDTO == null || requestDTO.getCategories() == null || requestDTO.getCategories().isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }


        Set<CategoryType> categories = new HashSet<>(requestDTO.getCategories());
        if(categories.contains(null)) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        //We find the agent and then validate it
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> {
                    log.error("Agent not found: {}", agentId);
                    return new IllegalArgumentException("Agent Not Found");
                });

        if(agent.getRole() != RoleType.AGENT) {
            log.warn("User {} is not an agent", agentId);
            throw new IllegalArgumentException("User is not an Agent");
        }

        //Then we set its expertise ---
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

    //We get all the agents ----
//This method is cached because agent data changes rarely compared to reads ----
//Instead of executing one count query per agent (N+1 problem),
//we fetch all ticket counts in a single query and store them in a HashMap.
    @Cacheable("agents")
    @Transactional()
    public List<AgentResponseDTO> getAgents() {

        List<StatusType> activeStatuses = List.of(
                StatusType.OPEN,
                StatusType.IN_PROGRESS
        );

        log.info("Fetching agents for admin assignment UI");

        //Fetch all agents in a single query
        List<User> agents = userRepository.findByRole(RoleType.AGENT);

        //Fetch active ticket counts for all agents in a single query
        List<Object[]> counts = ticketRepository.getAgentTicketCounts(activeStatuses);

        //Create a map: agentId -> active ticket count
        Map<String, Long> ticketCounts = new HashMap<>();

        for (Object[] row : counts) {
            User agent = (User) row[0];
            Long count = (Long) row[1];

            if (agent != null) {
                ticketCounts.put(agent.getId(), count);
            }
        }

        //Create response DTOs without triggering additional database queries
        return agents.stream()
                .map(agent -> new AgentResponseDTO(
                        agent.getId(),
                        agent.getName(),
                        agent.getEmail(),
                        agent.getExpertise(),
                        ticketCounts.getOrDefault(agent.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }
}
