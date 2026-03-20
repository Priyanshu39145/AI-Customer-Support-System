package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Services.TicketService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/tickets")
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
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable("ticketId") String ticketId, @RequestParam String agentId )   {
        return ResponseEntity.status(HttpStatus.OK).body(ticketService.assignTicket(ticketId,agentId));
    }

    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<TicketResponseDTO> changeStatus(@PathVariable("ticketId") String ticketId, @RequestParam StatusType status)  {
        return ResponseEntity.ok(ticketService.changeStatus(ticketId, status));
    }

}
