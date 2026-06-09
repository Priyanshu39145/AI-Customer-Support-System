package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import com.Spring.AI_Customer_Support_Backend_System.Security.JWTAuthFilter;
import com.Spring.AI_Customer_Support_Backend_System.Services.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    @Bean //Defining the security filter chain here ----
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable()) //This disables csrf protection ----
                //CSRF is cross site request forgery ---- useful when apps use browsers sessions and cookies ---
                //csrf protection is often disabled as modern APIs used stateless JWT authentication --- no browser sessions or cookies
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // Here we mention that the request must not create HTTP Sessions --- every request must authenticate independently using JWT.
                // CORS configuration - allow frontend origin and common headers ---
                //cors --- cross origin resource sharing --- it allows --- which frontend endpoint is allowed to call our backend APIs
                //I have given a custom cors configuration for it ---- go see corsConfigurationSource function ----
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //Here we define which endpoints will be given which access ----
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login/**").permitAll() //auth endpoints are accessed by all --- they are public
                        // Allow OPTIONS requests for all endpoints (CORS preflight)
                        //What is CORS preflight --- whenever a frontend app in a different host port tries to talk with the backend in another host port --- the browsers for protection sends a tester request with HTTP method OPTIONS to the backend  before the actual request goes --- This request is called CORS preflight request. So we allow the HTTP OPTIONS method for all requests ---- to allow CORS preflight requests to enter ---- Otherwise CORS error will be thrown by the browser
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
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
                            response.sendRedirect("http://localhost:5174/login?error=true");
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
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        // Allow common HTTP methods needed for CORS
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Allow common headers including Authorization for JWT token
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        // Allow credentials (cookies, auth headers) to be sent cross-origin
        configuration.setAllowCredentials(true);
        // Expose headers so frontend can access them
        configuration.setExposedHeaders(List.of("Authorization"));

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}