package com.Spring.AI_Customer_Support_Backend_System.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentAnalysisDTO implements Serializable {

    private boolean escalation;
    private boolean followUp;
    private double confidence;
    private String reason;

    public static IntentAnalysisDTO fallback(boolean hasExistingTicket) {
        return IntentAnalysisDTO.builder()
                .escalation(false)
                .followUp(false)
                .confidence(0.0)
                .reason("Intent analysis failed. Please try rephrasing your message or try again later.")
                .build();
    }
}
