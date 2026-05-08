package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketCommentResponseDTO implements Serializable {

    private String id;
    private String content;
    private String ticketId;
    private String authorId;
    private String authorName;
    private RoleType authorRole;
    private LocalDateTime createdAt;
}
