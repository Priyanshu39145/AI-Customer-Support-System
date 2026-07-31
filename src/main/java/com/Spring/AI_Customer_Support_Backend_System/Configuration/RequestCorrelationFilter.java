package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) //tells Spring to execute this filter before all other filters, so every subsequent filter and log already has the requestId available in the MDC.
public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Reuse request ID if provided by the client; otherwise generate one
        //We get the request id from the header
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        //if request id --- is blank ---- then we generate one for the request
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Store in MDC so every log statement automatically includes it
        //MDC (Mapped Diagnostic Context) is a thread-local storage that lets you attach data (like a request ID) to the current request so every log automatically includes it.
        MDC.put(MDC_REQUEST_ID_KEY, requestId);

        // Return the same request ID to the client
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear MDC to prevent data leaking to another request
            MDC.clear();
        }
    }
}