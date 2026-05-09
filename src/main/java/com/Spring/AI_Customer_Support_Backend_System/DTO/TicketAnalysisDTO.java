package com.Spring.AI_Customer_Support_Backend_System.DTO;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.CategoryType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.PriorityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketAnalysisDTO implements Serializable {

    private String title;
    private PriorityType priority;
    private CategoryType category;
    private String reason;

    public static TicketAnalysisDTO fallback(String message) {
        return TicketAnalysisDTO.builder()
                .title(defaultTitle(message))
                .priority(PriorityType.MEDIUM)
                .category(CategoryType.GENERAL)
                .reason("Fallback analysis used because AI classification could not be parsed.")
                .build();
    }

    private static String defaultTitle(String message) {
        if (message == null || message.isBlank()) {
            return "Customer Support Issue";
        }

        String normalized = message.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 50 ? normalized : normalized.substring(0, 50);
    }
}
