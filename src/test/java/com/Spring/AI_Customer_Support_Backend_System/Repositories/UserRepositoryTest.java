package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ProviderType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * @DataJpaTest starts only the JPA layer.
 *
 * It does NOT start:
 * - Controllers
 * - Services
 * - Security
 * - Redis
 * - AI models
 *
 * This makes repository tests very fast.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-repository;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})

/*
 * Use the H2 database defined above instead of
 * trying to connect to the application's real database.
 */
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    // Only these ticket statuses should count
    // as an agent's active workload.
    private static final List<StatusType> ACTIVE_STATUSES =
            List.of(StatusType.OPEN, StatusType.IN_PROGRESS);

    // Repository under test.
    @Autowired
    private UserRepository userRepository;

    /*
     * TestEntityManager lets us insert test data
     * directly into the in-memory database.
     *
     * Think of it as a helper for creating
     * database records during tests.
     */
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPreferCategoryExpertiseBeforeTicketLoadAndIgnoreClosedTickets() {

        // Create three agents with different expertise.
        User billingAgent =
                persistAgent("billing@example.com",
                        Set.of(CategoryType.BILLING));

        User technicalAgent =
                persistAgent("technical@example.com",
                        Set.of(CategoryType.TECHNICAL));

        User generalAgent =
                persistAgent("general@example.com",
                        Set.of());

        /*
         * Billing agent has one CLOSED ticket.
         * Closed tickets should NOT count as workload.
         */
        persistTicket(billingAgent, StatusType.CLOSED);

        /*
         * General agent has one OPEN ticket.
         * This DOES count as workload.
         */
        persistTicket(generalAgent, StatusType.OPEN);

        /*
         * Flush:
         * Sends all pending inserts to the database.
         *
         * Clear:
         * Removes cached entities so that the repository
         * performs an actual database query instead of
         * returning cached objects.
         */
        entityManager.flush();
        entityManager.clear();

        // Ask the repository for the least-loaded BILLING agent.
        List<User> result = userRepository.findLeastLoadedAgentsByCategory(
                RoleType.AGENT,
                CategoryType.BILLING,
                ACTIVE_STATUSES,
                PageRequest.of(0, 1));

        /*
         * Expected:
         *
         * Billing agent should be selected because:
         *
         * 1. They have BILLING expertise.
         * 2. Their only ticket is CLOSED,
         *    so active workload is zero.
         *
         * Even though another agent may have fewer tickets,
         * expertise filtering happens BEFORE workload comparison.
         */
        assertThat(result)
                .singleElement()
                .extracting(User::getEmail)
                .isEqualTo(billingAgent.getEmail());

        // Make sure non-billing agents were not returned.
        assertThat(result)
                .extracting(User::getEmail)
                .doesNotContain(
                        technicalAgent.getEmail(),
                        generalAgent.getEmail());
    }

    @Test
    void shouldReturnNoAgentWhenNoOneHasRequestedExpertise() {

        // No BILLING agent exists.
        persistAgent("technical@example.com",
                Set.of(CategoryType.TECHNICAL));

        persistAgent("general@example.com",
                Set.of());

        entityManager.flush();
        entityManager.clear();

        List<User> result = userRepository.findLeastLoadedAgentsByCategory(
                RoleType.AGENT,
                CategoryType.BILLING,
                ACTIVE_STATUSES,
                PageRequest.of(0, 1));

        // Since nobody has BILLING expertise,
        // the repository should return an empty list.
        assertThat(result).isEmpty();
    }

    @Test
    void shouldOrderAllAgentsByActiveTicketCountAscending() {

        // Agent with zero active tickets.
        User noActiveTickets =
                persistAgent("none@example.com",
                        Set.of(CategoryType.GENERAL));

        // Agent with one active ticket.
        User oneActiveTicket =
                persistAgent("one@example.com",
                        Set.of(CategoryType.GENERAL));

        // Agent with two active tickets.
        User twoActiveTickets =
                persistAgent("two@example.com",
                        Set.of(CategoryType.GENERAL));

        /*
         * Closed ticket should NOT increase workload.
         */
        persistTicket(noActiveTickets, StatusType.CLOSED);

        /*
         * One active ticket.
         */
        persistTicket(oneActiveTicket, StatusType.OPEN);

        /*
         * Two active tickets.
         */
        persistTicket(twoActiveTickets, StatusType.OPEN);
        persistTicket(twoActiveTickets, StatusType.IN_PROGRESS);

        entityManager.flush();
        entityManager.clear();

        // Fetch agents ordered by workload.
        List<User> result = userRepository.findLeastLoadedAgents(
                RoleType.AGENT,
                ACTIVE_STATUSES,
                PageRequest.of(0, 3));

        /*
         * Expected order:
         *
         * 0 active tickets
         * ↓
         * 1 active ticket
         * ↓
         * 2 active tickets
         */
        assertThat(result)
                .extracting(User::getEmail)
                .containsExactly(
                        noActiveTickets.getEmail(),
                        oneActiveTicket.getEmail(),
                        twoActiveTickets.getEmail());
    }

    /**
     * Helper method for creating an AGENT user.
     *
     * Every test needs valid User objects.
     * Instead of repeating the builder every time,
     * we centralize the creation here.
     *
     * Only the email and expertise change
     * from one test to another.
     */
    private User persistAgent(String email, Set<CategoryType> expertise) {

        return entityManager.persistAndFlush(
                User.builder()
                        .name(email.substring(0, email.indexOf('@')))
                        .email(email)
                        .password("encoded-password")
                        .role(RoleType.AGENT)
                        .expertise(expertise)
                        .enabled(true)
                        .providerType(ProviderType.EMAIL)
                        .build()
        );
    }

    /**
     * Helper method for creating tickets.
     *
     * The only thing we change in different tests
     * is the ticket status (OPEN, CLOSED, etc.).
     *
     * Everything else stays the same because it
     * isn't relevant to the repository logic
     * we're testing.
     */
    private void persistTicket(User assignedTo, StatusType status) {

        entityManager.persist(
                Ticket.builder()
                        .title("Support issue")
                        .description("Customer needs assistance")
                        .status(status)
                        .priority(PriorityType.MEDIUM)
                        .category(CategoryType.GENERAL)
                        .createdBy(assignedTo)
                        .assignedTo(assignedTo)
                        .build()
        );
    }
}