package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.TicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, String> {

    @Query("""
            SELECT activity FROM TicketActivity activity
            JOIN FETCH activity.performedBy
            WHERE activity.ticket.id = :ticketId
            ORDER BY activity.createdAt ASC
            """)
    List<TicketActivity> findByTicketIdWithPerformedByOrderByCreatedAtAsc(@Param("ticketId") String ticketId);
}
