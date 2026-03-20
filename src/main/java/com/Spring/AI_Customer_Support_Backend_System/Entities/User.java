package com.Spring.AI_Customer_Support_Backend_System.Entities;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users") //Provided as user is default table in many areas
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

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


}
