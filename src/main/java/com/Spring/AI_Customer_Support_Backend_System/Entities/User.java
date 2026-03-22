package com.Spring.AI_Customer_Support_Backend_System.Entities;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ProviderType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users") //Provided as user is default table in many areas
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false , unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    private boolean enabled;

    private String providerId;

    private ProviderType providerType;

    @JsonIgnore
    @OneToMany(mappedBy = "createdBy")
    private List<Ticket> createdTickets;

    @JsonIgnore
    @OneToMany(mappedBy = "assignedTo")
    private List<Ticket> assignedTickets;

    @JsonIgnore
    @OneToMany(mappedBy = "sender")
    private List<Message> messages;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // Returns the roles/permissions of the user for authorization.
    // Converts role (e.g., ADMIN, USER) into Spring Security format (ROLE_ADMIN, ROLE_USER).
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Defines the username used for login.
    // Here, email is used as the username instead of a separate username field.
    @Override
    public String getUsername() {
        return getEmail();
    }
}
