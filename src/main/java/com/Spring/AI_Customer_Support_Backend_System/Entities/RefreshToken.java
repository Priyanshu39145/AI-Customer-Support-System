package com.Spring.AI_Customer_Support_Backend_System.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    private LocalDateTime revokedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Optimistic locking version to prevent race conditions when multiple
    // concurrent refresh requests try to use the same token
    @Version
    private Long version;
}
//This is the entity which stores the refresh tokens inside the database ----
//The Refresh tokens are generated whenever a new login session has started --- whenever the JWT expires ----
// the refresh method automatically invokes --- and using the refresh token ---- anoher new JWT token is generated ---
//We have the id, token ---- the user object --- expiry time ---
//If we logout then we set the revoke as true --- which destroys the refresh token ---
//At the end we store version ---- to prevent race condition for concurrent refresh requests ----

