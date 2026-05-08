package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketActivityResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketDetailedResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreateTicketResponseDTO> createTicket( @Valid @RequestBody CreateTicketRequestDTO requestDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CategoryType category = requestDTO.getCategory() != null ? requestDTO.getCategory() : CategoryType.GENERAL;
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(user, requestDTO, null, PriorityType.MEDIUM, category));
    }

    @PutMapping("/tickets/{ticketId}/status")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<TicketResponseDTO> changeStatus(@PathVariable("ticketId") String ticketId, @RequestParam StatusType status)  {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.changeStatus(user,ticketId, status));
    }

    @PutMapping("/tickets/{ticketId}/priority")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<TicketResponseDTO> changePriority(@PathVariable("ticketId") String ticketId,
                                                            @RequestParam PriorityType priority) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.changePriority(user, ticketId, priority));
    }

    @PutMapping("/tickets/{ticketId}/category")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<TicketResponseDTO> changeCategory(@PathVariable("ticketId") String ticketId,
                                                            @RequestParam CategoryType category) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.changeCategory(user, ticketId, category));
    }

    @PutMapping("/tickets/{ticketId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable("ticketId") String ticketId,
                                                          @RequestParam String agentId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.assignTicket(user, ticketId, agentId));
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketDetailedResponseDTO> getTicketById(@PathVariable String ticketId)   {
        //This statement gives us all the details of the current user including the role ---
        //We have to set /doctors as request matchers to only roles containing doctor ---
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.getTicketById(ticketId,user));
    }

    @GetMapping("/tickets/{ticketId}/history")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public ResponseEntity<List<TicketActivityResponseDTO>> getTicketHistory(@PathVariable String ticketId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.getTicketHistory(ticketId, user));
    }

    @GetMapping("/users/me/tickets")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public ResponseEntity<Page<TicketResponseDTO>> getTicketsOfUser(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) StatusType status,
                                                                    @RequestParam(required = false) PriorityType priority,
                                                                    @RequestParam(required = false) CategoryType category,
                                                                    @RequestParam(required = false) String assignedToId,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo)    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.getTicketsOfUser(user,keyword,status,priority,category,assignedToId,createdFrom,createdTo,page,size));
    }


}
