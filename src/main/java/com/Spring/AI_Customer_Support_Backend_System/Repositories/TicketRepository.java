package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, String> {
    Page<Ticket> findAll(Pageable pageable);


    @Query("""
    SELECT t FROM Ticket t
    WHERE
        (t.createdBy = :user)
        AND (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:category IS NULL OR t.category = :category)
        AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)
        AND (:createdFrom IS NULL OR t.createdAt >= :createdFrom)
        AND (:createdTo IS NULL OR t.createdAt <= :createdTo)
    """)
    Page<Ticket> searchTicketsBelongingToUser(@Param("user") User user,
                                              @Param("keyword") String keyword,
                                              @Param("status") StatusType status,
                                              @Param("priority") PriorityType priority,
                                              @Param("category") CategoryType category,
                                              @Param("assignedToId") String assignedToId,
                                              @Param("createdFrom") LocalDateTime createdFrom,
                                              @Param("createdTo") LocalDateTime createdTo,
                                              Pageable pageable);

    @Query("""
    SELECT t FROM Ticket t
    WHERE
        (t.assignedTo = :user)
        AND (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:category IS NULL OR t.category = :category)
        AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)
        AND (:createdFrom IS NULL OR t.createdAt >= :createdFrom)
        AND (:createdTo IS NULL OR t.createdAt <= :createdTo)
    """)
    Page<Ticket> searchTicketsBelongingToAgent(@Param("user") User user,
                                              @Param("keyword") String keyword,
                                              @Param("status") StatusType status,
                                              @Param("priority") PriorityType priority,
                                              @Param("category") CategoryType category,
                                              @Param("assignedToId") String assignedToId,
                                              @Param("createdFrom") LocalDateTime createdFrom,
                                              @Param("createdTo") LocalDateTime createdTo,
                                              Pageable pageable);

    @Query("""
    SELECT t FROM Ticket t
    WHERE
        (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:category IS NULL OR t.category = :category)
        AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)
        AND (:createdFrom IS NULL OR t.createdAt >= :createdFrom)
        AND (:createdTo IS NULL OR t.createdAt <= :createdTo)
    """)
    Page<Ticket> searchAllTickets(@Param("keyword") String keyword,
                                  @Param("status") StatusType status,
                                  @Param("priority") PriorityType priority,
                                  @Param("category") CategoryType category,
                                  @Param("assignedToId") String assignedToId,
                                  @Param("createdFrom") LocalDateTime createdFrom,
                                  @Param("createdTo") LocalDateTime createdTo,
                                  Pageable pageable);

    //Searching by keyword only --- we use Query method for it ---
    @Query("""
    SELECT t FROM Ticket t
    WHERE
        (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Ticket> searchTickets(String keyword, Pageable pageable);
    //Searching by keyword and status
    @Query("""
    SELECT t FROM Ticket t
    WHERE 
        (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND t.status = :status
    """)
    Page<Ticket> searchTicketsByStatus(String keyword, StatusType status, Pageable pageable);
    //Searching by keyword and priority
    @Query("""
    SELECT t FROM Ticket t
    WHERE 
        (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND t.priority = :priority
    """)
    Page<Ticket> searchTicketsByPriority(String keyword, PriorityType priority, Pageable pageable);

    @Query("""
    SELECT t FROM Ticket t
    WHERE 
        (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND t.status = :status
            AND t.priority = :priority
    """)
    Page<Ticket> searchTicketsByStatusAndPriority(String keyword, StatusType status, PriorityType priority, Pageable pageable);



    @Query("""
    SELECT t.assignedTo, COUNT(t)
    FROM Ticket t
    WHERE t.status IN :statuses
    GROUP BY t.assignedTo
""")
    List<Object[]> getAgentTicketCounts(@Param("statuses") List<StatusType> statuses);

    int countByCreatedByAndCreatedAtAfter(User user, LocalDateTime todayStart);

    Optional<Ticket> findTopByCreatedByOrderByCreatedAtDesc(User user);

    long countByStatus(StatusType status);

    long countByPriority(PriorityType priority);

    long countByAssignedToAndStatusIn(User assignedTo, List<StatusType> statuses);

    long countByAssignedTo(User assignedTo);

    long countByCreatedBy(User createdBy);

    long countByCreatedByAndStatus(User createdBy, StatusType status);

    long countByCreatedByAndPriority(User createdBy, PriorityType priority);

    long countByAssignedToAndStatus(User assignedTo, StatusType status);

    long countByAssignedToAndPriority(User assignedTo, PriorityType priority);

    long countByAssignedToId(String agentId);

    long countByAssignedToIdAndStatus(String agentId, StatusType status);
}
