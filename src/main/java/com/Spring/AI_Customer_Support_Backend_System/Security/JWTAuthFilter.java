package com.Spring.AI_Customer_Support_Backend_System.Security;

import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * PRODUCTION FIX #5: Bypass auth endpoints
 * - /auth/refresh uses REFRESH TOKEN only, not access token
 * - This endpoint should not require JWT validation
 * - Without this bypass, the filter would try to validate JWT
 *   but /auth/refresh sends refresh token in body, not JWT in header
 * - This would cause 401 errors on refresh attempts
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class JWTAuthFilter extends OncePerRequestFilter { //OncePerRequestFilter ---- This filter runs ONLY ONCE per request.

    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Endpoints that should bypass JWT authentication.
     * These endpoints either don't need auth or use different auth mechanisms.
     */
    //These endpoints are public and dont need JWT Authentication ---
    private static final String[] BYPASS_PATHS = {
        "/auth/refresh",  // Uses refresh token in request body
        "/auth/logout",   // Session handled differently
        "/auth/login",   // No auth needed
        "/auth/register", // Registration doesn't need JWT
        "/oauth2/",    // OAuth2 flow endpoints
    };

    /**
     * Check if request should bypass JWT filter.
     */
    //This method checks if the incoming request should be bypassed or not according to the rules ---
    private boolean shouldBypass(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Check bypass paths
        for (String bypassPath : BYPASS_PATHS) {
            if (path.contains(bypassPath)) {
                return true;
            }
        }

        return false;
    }

    @Override //Here Spring Security internally asks --- should I skip this request --- and I answer by returning true or false according to should bypass or not
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return shouldBypass(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.debug("JWTAuthFilter processing: {}", request.getRequestURI());

            // Get Authorization header
            final String header = request.getHeader("Authorization");

            // No valid Authorization header - let it pass through
            // Security config will handle unauthorized access
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract JWT token (everything after "Bearer ")
            String token = header.substring(7);

            // Get username from token
            //This method internally validates the token using the secret key and then it returns the user from the token ---
            String username = authUtil.getUserNamefromToken(token); // --- See the getUserNamefromToken method in authUtil ---

            if (username == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check if SecurityContext already has authentication
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user from database
                User user = userRepository.findByEmail(username).orElse(null);

                if (user != null) {
                    // Create authentication token with user's authorities --- and then set it inside the SecurityContextHolder
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User authenticated: {}", username);
                }
            }

            // Continue with filter chain
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWTAuthFilter error: {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}

//Complete Flow ---
/*
Incoming Request
       ↓
JWTAuthFilter
       ↓
Extract Authorization header
       ↓
Extract JWT token
       ↓
Extract username
       ↓
Load user from DB
       ↓
Create Authentication object
       ↓
Store inside SecurityContext
       ↓
Spring Security now trusts user
       ↓
Controller access granted
 */