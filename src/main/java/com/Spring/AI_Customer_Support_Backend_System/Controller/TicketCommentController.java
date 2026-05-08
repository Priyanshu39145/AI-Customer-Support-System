package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketCommentRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.TicketCommentResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Services.TicketCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    @GetMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public ResponseEntity<List<TicketCommentResponseDTO>> getCommentsForTicket(@PathVariable String ticketId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketCommentService.getCommentsForTicket(ticketId, user));
    }

    @PostMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public ResponseEntity<TicketCommentResponseDTO> addComment(@PathVariable String ticketId,
                                                               @Valid @RequestBody TicketCommentRequestDTO requestDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketCommentService.addComment(ticketId, user, requestDTO));
    }
}
