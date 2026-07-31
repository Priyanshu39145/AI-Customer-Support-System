package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import com.Spring.AI_Customer_Support_Backend_System.Security.JWTAuthFilter;
import com.Spring.AI_Customer_Support_Backend_System.Services.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;
import java.util.List;

@Configuration //Creates a Bean for Spring processing
@RequiredArgsConstructor
@EnableMethodSecurity //It enables annotations like @PreAuthorize for handling roles and @Secured
@Slf4j
public class SecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;
    private final oAuth2SuccessHandler oauth2successHandler;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RateLimitFilter rateLimitFilter;
    private final Environment environment; //Used to read application.properties env variables ----
    private final RequestCorrelationFilter requestCorrelationFilter;

    @Bean //Defining the security filter chain here ----
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(csrfTokenRepository())
//                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
//                ) //Previously there was csrf.disable --- THis is because ---- JWT in Authorization header is immune to CSRF. Cookies are not. Therefore once authentication moved into cookies, CSRF protection became mandatory.
                .csrf(csrf -> csrf.disable())
                //Now Spring creates XSRF-TOKEN cookie. Frontend reads it.
                //Every Post sends X-XSRF-TOKEN header. Spring compares them both IF mismatch --- Request rejected
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // Here we mention that the request must not create HTTP Sessions --- every request must authenticate independently using JWT.
                // CORS configuration - allow frontend origin and common headers ---
                //cors --- cross origin resource sharing --- it allows --- which frontend endpoint is allowed to call our backend APIs
                //I have given a custom cors configuration for it ---- go see corsConfigurationSource function ----
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //Here we define which endpoints will be given which access ----
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/auth/logout", "/auth/csrf").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/logout", "/api/auth/csrf").permitAll()
                        .requestMatchers("/auth/oauth2/**", "/login/oauth2/**").permitAll()
                        // Allow OPTIONS requests for all endpoints (CORS preflight)
                        //What is CORS preflight --- whenever a frontend app in a different host port tries to talk with the backend in another host port --- the browsers for protection sends a tester request with HTTP method OPTIONS to the backend  before the actual request goes --- This request is called CORS preflight request. So we allow the HTTP OPTIONS method for all requests ---- to allow CORS preflight requests to enter ---- Otherwise CORS error will be thrown by the browser
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        //Now we allow specific endpoints to be allowed at specific roles ----
                        //What Spring does internally here? --- it first matches the URL --- and then inside the Authentication object generated during the Auth process (discussed later) it find for the list of GrantedAuthorities type where it has objects of SimpleGrantedAuthority where Spring finds for keyword ROLE_USER or ROLE_ADMIN or ROLE_AGENT ---- like that
                        .requestMatchers("/conversations/**", "/messages/**").hasRole("USER")
                        .requestMatchers("/agents/{agentId}/categories", "/upload").hasRole("ADMIN")
                        .requestMatchers("/tickets/{ticketId}/assign").hasRole("ADMIN")
                        .requestMatchers("/tickets/{ticketId}/status").hasRole("AGENT")
                        .requestMatchers("/tickets/{ticketId}/priority").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers("/tickets/{ticketId}/category").hasAnyRole("AGENT", "ADMIN")
                        //We make any other request authenticated ---- based on the PreAuthorize tag --- or internal backend service method logic ---
                        .anyRequest().authenticated()
                )
                //We disable the default login page given by Spring Security
                .formLogin(form -> form.disable())
                //We disable the basic auth given by Spring Security ---
                .httpBasic(basic -> basic.disable())
                // Skip JWT filter for OPTIONS requests to avoid blocking CORS preflight
                //Now comes filters ---- The request goes through first RateLimitFilter for preventing high request traffic and then goes through JWTFilter for JWT Authentication ----
                //They are both before the UsernamePasswordAuthenticationFilter --- which means we override the default Spring username and password filter with our JWTFilter ---
                .addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                //Now JWT is done --- now we mention --- the oAuth2login endpoint ----
                .oauth2Login(oAuth2 -> oAuth2
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/auth/oauth2/authorization")
                        )
                        //We mention a failure handler ---- for failure --- we display a log here --- but it is not good --- so we redirect back to the login page ---
                        .failureHandler(((request, response, exception) -> {
//                            log.error("OAuth2 Error: {}", exception.getMessage());
                            //Previously the frontend url was hardcoded ---- now we get it from env
                            response.sendRedirect(environment.getProperty("app.frontend.url", "http://localhost:5174") + "/login");
                        }))
                        //For success ---- oauth2successHandler has all the logic for authentication for oauth2login Request
                        .successHandler(oauth2successHandler))
                //Now we mention the access denied handler of the project ---
                //Access denied happens when --- the request authenticates --- but the role is not right ----
                //handler exception resolver --- checks the 403 forbidden error efficiently ---
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.accessDeniedHandler((request, response, accessDeniedException) -> {
                    handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
                }))
                //Now we handle authentication failure ---- by 401 Unauthorized -----
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                );

        return httpSecurity.build();
    }

    // CORS configuration allowing frontend origin with all common methods and headers ---

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow frontend origins from application.properties
        String allowedOrigins = environment.getProperty("app.cors.allowed-origins", "http://localhost:5174");
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        // Allow common HTTP methods needed for CORS
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        //We allow the CSRF token created for CORS
        configuration.setAllowedHeaders(List.of("X-Request-ID", "Content-Type", "X-Requested-With", "Accept", "X-XSRF-TOKEN"));
        // Allow credentials (cookies, auth headers) to be sent cross-origin
        configuration.setAllowCredentials(true);
        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    //We configure the CookieCsrfTokenRepository here ---- meaning we configure how Spring stores CSRF tokens
    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        String csrfCookieDomain = environment.getProperty("app.auth.csrf-cookie.domain", "");
        repository.setCookieCustomizer(cookie -> {
            cookie.secure(environment.getProperty("app.auth.cookie.secure", Boolean.class, false))
                    .sameSite(environment.getProperty("app.auth.cookie.same-site", "Lax"));
            if (!csrfCookieDomain.isBlank()) {
                cookie.domain(csrfCookieDomain);
            }
        });
        return repository;
    }
}
