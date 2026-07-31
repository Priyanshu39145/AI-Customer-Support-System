package com.Spring.AI_Customer_Support_Backend_System.Security;

import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
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
 * - /auth/refresh uses the refresh-token cookie, not an access token
 * - This endpoint should not require JWT validation
 * - Without this bypass, the filter would try to validate JWT
 *   but /auth/refresh uses a different HttpOnly cookie
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
        "/auth/refresh",
        "/auth/logout",
        "/auth/login",
        "/auth/register",
        "/auth/csrf",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/csrf",
        "/oauth2/",
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
            //Instead of extracting header ---- we get the access token from the cookie ---
            String token = getCookieValue(request, AuthenticationCookieService.ACCESS_TOKEN_COOKIE);
            log.info("JWT cookie present: {}", token != null);

            // No access-token cookie - let Spring Security return the authentication error.
            // Security config will handle unauthorized access
            if (token == null || token.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get username from token
            //This method internally validates the token using the secret key and then it returns the user from the token ---
            String username = authUtil.getUserNamefromToken(token); // --- See the getUserNamefromToken method in authUtil ---
            log.info("Username from JWT: {}", username);
            if (username == null) {
                filterChain.doFilter(request, response);
                return;
            }


            // Check if SecurityContext already has authentication
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                log.info("SecurityContext before auth: {}",
                        SecurityContextHolder.getContext().getAuthentication());
                // Load user from database
                User user = userRepository.findByEmail(username).orElse(null);
                //IF user is null or it is not enabled ---- then authentication is not allowrd
                if (user != null && user.isEnabled()) {
                    // Create authentication token with user's authorities --- and then set it inside the SecurityContextHolder
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User authenticated: {}", username);
                    log.info("User found: {}", user != null ? user.getEmail() : "null");
                    log.info("Authentication set for {}", username);
                    log.info("SecurityContext after auth: {}",
                            SecurityContextHolder.getContext().getAuthentication());
                }
            }

            // Continue with filter chain
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWTAuthFilter error: {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
