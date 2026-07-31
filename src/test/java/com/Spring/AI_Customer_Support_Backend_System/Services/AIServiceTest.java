package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.IntentAnalysisDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AIServiceTest {

    // We only want to test the normalization logic.
    // normalizeIntent() does NOT call the AI model,
    // so a mocked ChatClient is enough.
    private final AIService aiService = new AIService(mock(ChatClient.class), new ObjectMapper());

    @Test
    void shouldFlipEscalationToFollowUpWhenTicketAlreadyExists() {

        // AI predicts that the message should be escalated.
        IntentAnalysisDTO analysis = analysis(true, false, 0.9, "Customer needs assistance");

        // 'true' means the customer already has an existing ticket.
        IntentAnalysisDTO result = aiService.normalizeIntent(analysis, true);

        // Business Rule:
        // A customer cannot create another escalation
        // if a ticket already exists.
        // Instead, it becomes a follow-up request.
        assertThat(result.isEscalation()).isFalse();
        assertThat(result.isFollowUp()).isTrue();
    }

    @Test
    void shouldDisableFollowUpWhenThereIsNoExistingTicket() {

        // AI predicts this is a follow-up.
        IntentAnalysisDTO analysis = analysis(false, true, 0.9, "Customer asked for an update");

        // 'false' means no existing ticket.
        IntentAnalysisDTO result = aiService.normalizeIntent(analysis, false);

        // Business Rule:
        // You cannot follow up on a ticket
        // that doesn't exist.
        assertThat(result.isFollowUp()).isFalse();
    }

    @Test
    void shouldReturnFallbackWhenAnalysisIsNull() {

        // Simulate the AI completely failing
        // and returning no analysis.
        IntentAnalysisDTO result = aiService.normalizeIntent(null, true);

        // This is what the service should return
        // when the AI analysis is unavailable.
        IntentAnalysisDTO fallback = IntentAnalysisDTO.fallback(true);

        // Compare individual fields instead of object references.
        // We only care that the values are identical.
        assertThat(result.isEscalation()).isEqualTo(fallback.isEscalation());
        assertThat(result.isFollowUp()).isEqualTo(fallback.isFollowUp());
        assertThat(result.getConfidence()).isEqualTo(fallback.getConfidence());
        assertThat(result.getReason()).isEqualTo(fallback.getReason());
    }

    // Run this same test for every invalid confidence value
    // returned by outOfRangeConfidenceValues().
    @ParameterizedTest
    @MethodSource("outOfRangeConfidenceValues")
    void shouldClampConfidenceToSupportedRange(double confidence) {

        IntentAnalysisDTO result = aiService.normalizeIntent(
                analysis(false, false, confidence, "A reason"), false);

        // Regardless of the AI's output,
        // confidence must always stay between 0 and 1.
        assertThat(result.getConfidence()).isBetween(0.0, 1.0);

        // Invalid numeric values should become 0.
        if (Double.isNaN(confidence)
                || Double.isInfinite(confidence)
                || confidence < 0.0) {

            assertThat(result.getConfidence()).isZero();
        }

        // Values greater than 1 should become exactly 1.
        if (confidence > 1.0 && !Double.isInfinite(confidence)) {
            assertThat(result.getConfidence()).isEqualTo(1.0);
        }
    }

    /**
     * Supplies invalid confidence values
     * for the parameterized test above.
     *
     * The test will automatically execute once
     * for each value in this stream.
     */
    private static Stream<Double> outOfRangeConfidenceValues() {
        return Stream.of(
                1.5,
                -0.25,
                Double.NaN,
                Double.POSITIVE_INFINITY
        );
    }

    @Test
    void shouldSupplyDefaultReasonWhenReasonIsNullOrBlank() {

        // AI forgot to provide a reason.
        IntentAnalysisDTO nullReason =
                aiService.normalizeIntent(
                        analysis(false, false, 0.5, null),
                        false
                );

        // AI provided an empty/blank reason.
        IntentAnalysisDTO blankReason =
                aiService.normalizeIntent(
                        analysis(false, false, 0.5, "   "),
                        false
                );

        // Instead of returning null or an empty string,
        // the service should always provide a meaningful default.
        assertThat(nullReason.getReason())
                .isEqualTo("Intent analysis completed.");

        assertThat(blankReason.getReason())
                .isEqualTo("Intent analysis completed.");
    }

    /**
     * Helper method to create IntentAnalysisDTO objects.
     *
     * Instead of repeating:
     *
     * IntentAnalysisDTO.builder()
     *      ...
     *      .build();
     *
     * in every test, we call this helper.
     *
     * This keeps every test short and makes it clear
     * which values are important for that specific test.
     */
    private IntentAnalysisDTO analysis(
            boolean escalation,
            boolean followUp,
            double confidence,
            String reason) {

        return IntentAnalysisDTO.builder()
                .escalation(escalation)
                .followUp(followUp)
                .confidence(confidence)
                .reason(reason)
                .build();
    }
}