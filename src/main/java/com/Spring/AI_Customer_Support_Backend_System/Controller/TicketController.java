package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketDetailedResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Services.TicketService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreateTicketResponseDTO> createTicket(@RequestParam String userId, @RequestBody CreateTicketRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(userId,requestDTO));
    }

    @GetMapping("/tickets")
    public ResponseEntity<Page<TicketResponseDTO>> getTicketByStatusAndPriority(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) StatusType status,
            @RequestParam(required = false)PriorityType priority)   {
        return ResponseEntity.ok(ticketService.getTicketByStatusAndPriority(status,priority,page,size));
    }

    @PutMapping("/tickets/{ticketId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable("ticketId") String ticketId, @RequestParam String agentId )   {
        return ResponseEntity.status(HttpStatus.OK).body(ticketService.assignTicket(ticketId,agentId));
    }

    @PutMapping("/tickets/{ticketId}/status")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<TicketResponseDTO> changeStatus(@PathVariable("ticketId") String ticketId, @RequestParam StatusType status)  {
        return ResponseEntity.ok(ticketService.changeStatus(ticketId, status));
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketDetailedResponseDTO> getTicketById(@PathVariable String ticketId)   {
        //This statement gives us all the details of the current user including the role ---
        //We have to set /doctors as request matchers to only roles containing doctor ---
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.getTicketById(ticketId,user));
    }

}
