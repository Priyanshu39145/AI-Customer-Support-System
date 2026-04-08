package com.Spring.AI_Customer_Support_Backend_System.Entities;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.SenderType;
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
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "ticket_id" , nullable = true)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    private SenderType sender;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = true)
    private User senderUser;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
