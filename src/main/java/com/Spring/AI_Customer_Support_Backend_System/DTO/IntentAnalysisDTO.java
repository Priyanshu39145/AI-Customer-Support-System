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

//This DTO stores the AI response about the Intent Analysis ---
//escalation is true if the AI is sure that the USer has a legitimate problem and needs a ticket created ---
//followUp is true when the AI is sure that the User wants the details of his previous tickets
//confidence is the score on the basis of which we apply deterministic Backend routing ---
//reason --- is the AI generated text which mentions why we have escalation , followUp and confidence like that

//fallback is used when the Intent Analysis by the AI fails --- then we set the IntentAnalysisDTO to fallback ---
