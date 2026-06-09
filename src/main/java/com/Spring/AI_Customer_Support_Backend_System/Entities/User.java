package com.Spring.AI_Customer_Support_Backend_System.Entities;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users") //Provided as user is default table in many areas
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails { //implementing UserDetails tells Spring Security that this entity can be used for authentication

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
    @Column(nullable = false)
    private RoleType role;

    @ElementCollection(fetch = FetchType.EAGER) //This creates another table immediatly --- where each user is given a category ----
    @CollectionTable(name = "user_expertise", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Builder.Default
    private Set<CategoryType> expertise = new HashSet<>();

    private boolean enabled;

    //ProviderId and ProviderType are for oAuth2 ---
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;

    //@JsonIgnore is used to avoid huge responses ----

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
    //We override the getUsername method of UserDetails interface to make it give Email whenever username is asked ---
    @Override
    public String getUsername() {
        return getEmail();
    }
}


//Here is the main User entity ---- it first has the id,name,email and role(ADMIN, USER or AGENT) ----
//Then comes expertise ---- which defines the Category of Agent for efficient ticket classification ----
//Then comes enabled --- to provide soft delete 0000
//Then comes providerId ---- for oAuth2 login ----
//providerType ---- either GOOGLE or EMAIL ----
//Then we have the created tickets of the User --- and the assignedTickets for Agent ----
// Relationship is User -> Many Tickets --- (Ticket is owner of the relationship)

