package com.Spring.AI_Customer_Support_Backend_System.Entities;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ConversationStatusType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConversationStatusType status = ConversationStatusType.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL , fetch = FetchType.LAZY , orphanRemoval = true)
    private List<Message> messages;

    @OneToOne(mappedBy = "conversation")
    private Ticket ticket;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
//This Conversation entity consists of the id, title of the conversation --- ConversationStatus --- which is by default ACTIVE
//Then deleted flag for deleting conversations ----
//The user of the conversation ---
//And the messages of the conversation --- we fetch it LAZY ---
//The ticket made through the conversation (only one can be made) ---
