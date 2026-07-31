package com.Spring.AI_Customer_Support_Backend_System.Security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service responsible for creating and removing authentication cookies.
 * It stores the JWT access token and refresh token in HttpOnly cookies.
 */
@Service
public class AuthenticationCookieService {

    // Names of the cookies that will be stored in the browser
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    // Whether cookies should only be sent over HTTPS ---- default is false ---- cookies sent over HTTP
    @Value("${app.auth.cookie.secure:false}")
    private boolean secure;

    // Controls when the browser sends cookies (Lax, Strict, None)
    //Lax ---- The cookie is sent for normal navigation within your site and when the user clicks a link to your site. This provides good protection against CSRF attacks while still working for most applications.
    //Strict ---- The cookie is only sent when the user is already on your website. It's the most secure option but can sometimes make navigation less convenient.
    //None ---- The cookie is sent in all situations, including cross-site requests. If you use None, the cookie must also have Secure=true (HTTPS only).
    @Value("${app.auth.cookie.same-site:Lax}")
    private String sameSite;

    // Optional domain that the cookies belong to
    // If it's blank (your current configuration), the cookie is only available to the current domain (e.g., api.example.com).
    // f you set it to something like: .example.com. then the cookie is shared across all subdomains, such as: api.example.com, app.example.com, admin.example.com
    @Value("${app.auth.cookie.domain:}")
    private String domain;

    // Lifetime of the access token cookie (in minutes)
    @Value("${jwt.accessTokenExpirationMinutes:15}")
    private long accessTokenExpirationMinutes;

    // Lifetime of the refresh token cookie (in days)
    @Value("${jwt.refreshTokenExpirationDays:7}")
    private long refreshTokenExpirationDays;

    /**
     * Adds both authentication cookies to the response after login.
     */
    public void setAuthenticationCookies(HttpServletResponse response, String accessToken, String refreshToken) {

        // Prevent browsers from caching responses containing authentication cookies
        //If browsers cache cookies ---- then cookies can get exposed ----
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        // Store the access token cookie
        addCookie(response, ACCESS_TOKEN_COOKIE, accessToken,
                Duration.ofMinutes(accessTokenExpirationMinutes));

        // Store the refresh token cookie
        addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken,
                Duration.ofDays(refreshTokenExpirationDays));
    }

    /**
     * Removes both authentication cookies during logout.
     */
    public void clearAuthenticationCookies(HttpServletResponse response) {

        // Prevent caching of the logout response
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        // Overwrite cookies with empty values and expire them immediately
        addCookie(response, ACCESS_TOKEN_COOKIE, "", Duration.ZERO);
        addCookie(response, REFRESH_TOKEN_COOKIE, "", Duration.ZERO);
    }

    /**
     * Creates a cookie with the required security settings
     * and adds it to the HTTP response.
     */
    private void addCookie(HttpServletResponse response, String name, String value, Duration maxAge) {

        // Build the cookie with common security settings
        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(name, value)
                .httpOnly(true)          // JavaScript cannot access this cookie
                .secure(secure)          // Send only over HTTPS if enabled
                .sameSite(sameSite)      // Helps protect against CSRF attacks
                .path("/")               // Cookie is available throughout the application
                .maxAge(maxAge);         // How long the cookie remains valid

        // Set the cookie domain if one is configured
        if (domain != null && !domain.isBlank()) {
            cookie.domain(domain);
        }

        // Add the cookie to the HTTP response
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.build().toString());
    }
}
