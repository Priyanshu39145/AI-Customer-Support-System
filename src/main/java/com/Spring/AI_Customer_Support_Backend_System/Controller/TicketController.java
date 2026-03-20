package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Services.TicketService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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



}
