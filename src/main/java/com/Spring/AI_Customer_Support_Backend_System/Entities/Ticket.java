package com.Spring.AI_Customer_Support_Backend_System.Entities;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.StatusType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String title;
    //Description can be long --- this prevents DB truncation issues ---
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityType priority;

    @ManyToOne
    @JoinColumn(name="created_by", nullable = false)
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "assigned_to") //Tickets can exists without any Agent
    private User assignedTo;


    @JsonIgnore
    @OneToMany(mappedBy = "ticket" , fetch = FetchType.LAZY)
    private List<Message> messages;


    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
