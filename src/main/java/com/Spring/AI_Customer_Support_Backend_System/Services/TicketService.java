package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.CreateTicketResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final ModelMapper modelMapper;

    public CreateTicketResponseDTO createTicket(String userId, CreateTicketRequestDTO requestDTO)    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Ticket ticket = Ticket.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .status(StatusType.OPEN)
                .priority(requestDTO.getPriority() != null ? requestDTO.getPriority() : PriorityType.MEDIUM)
                .createdBy(user)
                .build();
        ticketRepository.save(ticket);

        return modelMapper.map(ticket , CreateTicketResponseDTO.class);
    }
}
