package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ProviderType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {


    Optional<User> findByEmail(String email);

    Optional<User> findByProviderIdAndProviderType(String providerId, ProviderType authProviderType);

    List<User> findByRole(RoleType role);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.role = :role
            ORDER BY (
                SELECT COUNT(t)
                FROM Ticket t
                WHERE t.assignedTo = u
                  AND t.status IN :statuses
            ) ASC
            """)
    List<User> findLeastLoadedAgents(@Param("role") RoleType role,
                                     @Param("statuses") List<StatusType> statuses,
                                     Pageable pageable);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.role = :role
              AND :category MEMBER OF u.expertise
            ORDER BY (
                SELECT COUNT(t)
                FROM Ticket t
                WHERE t.assignedTo = u
                  AND t.status IN :statuses
            ) ASC
            """)
    List<User> findLeastLoadedAgentsByCategory(@Param("role") RoleType role,
                                               @Param("category") CategoryType category,
                                               @Param("statuses") List<StatusType> statuses,
                                               Pageable pageable);
}
//Done
