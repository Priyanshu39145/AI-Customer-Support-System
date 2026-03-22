package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, String> {
    Page<Ticket> findAll(Pageable pageable);
    Page<Ticket> findByStatus(StatusType status, Pageable pageable);
    Page<Ticket> findByPriority(PriorityType priorityType, Pageable pageable);
    Page<Ticket> findByStatusAndPriority(StatusType status, PriorityType priority, Pageable pageable);

    Page<Ticket> findByCreatedBy(User user, Pageable pageable);

    Page<Ticket> findByCreatedByAndStatus(User user, StatusType status, Pageable pageable);

    Page<Ticket> findByCreatedByAndPriority(User user, PriorityType priority, Pageable pageable);

    Page<Ticket> findByCreatedByAndStatusAndPriority(User user, StatusType status, PriorityType priority, Pageable pageable);

    Page<Ticket> findByAssignedTo(User agent, Pageable pageable);
    Page<Ticket> findByAssignedToAndStatus(User agent, StatusType status, Pageable pageable);

    Page<Ticket> findByAssignedToAndPriority(User agent, PriorityType priority, Pageable pageable);

    Page<Ticket> findByAssignedToAndStatusAndPriority(User agent, StatusType status, PriorityType priority, Pageable pageable);
}