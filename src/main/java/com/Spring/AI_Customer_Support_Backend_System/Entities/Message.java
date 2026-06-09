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

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;


    @Enumerated(EnumType.STRING)
    private SenderType sender;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = true)
    private User senderUser;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
//A MEssage inside an USER to AI chat has following info --- id, content, conversation with which it is associated ---
// --- the senderType (USER or AI), the User entity inside the conversation
//createdAt for the time ---
