package com.Spring.AI_Customer_Support_Backend_System.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO implements Serializable {

    private String conversationId;
    private String conversationTitle;
}
