package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, String> {

    @Query("""
            SELECT comment FROM TicketComment comment
            JOIN FETCH comment.author
            WHERE comment.ticket.id = :ticketId
            ORDER BY comment.createdAt ASC
            """)
    List<TicketComment> findByTicketIdWithAuthorOrderByCreatedAtAsc(@Param("ticketId") String ticketId);
}
//Done
