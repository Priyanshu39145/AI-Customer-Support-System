package com.Spring.AI_Customer_Support_Backend_System.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "ticket_id" , nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User sender;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
