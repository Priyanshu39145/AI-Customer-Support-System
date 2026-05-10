package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AgentCategoriesResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.AgentResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.AssignAgentCategoriesRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.Services.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PutMapping("/agents/{agentId}/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentCategoriesResponseDTO> assignCategories(@PathVariable String agentId,
                                                                       @Valid @RequestBody AssignAgentCategoriesRequestDTO requestDTO) {
        return ResponseEntity.ok(agentService.assignCategories(agentId, requestDTO));
    }

    @GetMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgentResponseDTO>> getAgents() {
        return ResponseEntity.ok(agentService.getAgents());
    }
}
