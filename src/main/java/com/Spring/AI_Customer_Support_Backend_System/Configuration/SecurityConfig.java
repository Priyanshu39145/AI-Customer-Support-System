package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import com.Spring.AI_Customer_Support_Backend_System.Security.JWTAuthFilter;
import com.Spring.AI_Customer_Support_Backend_System.Services.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;
    private final oAuth2SuccessHandler oauth2successHandler;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception  {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //We have allowed auth and login endpoints full permission
                //We have authenticated all others
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login/**" ).permitAll()
                        .requestMatchers("/conversations/**", "/messages/**").hasRole("USER")
                        .requestMatchers("/agents/{agentId}/categories" , "/upload").hasRole("ADMIN")
                        .requestMatchers("/tickets/{ticketId}/assign").hasRole("ADMIN")
                        .requestMatchers("/tickets/{ticketId}/status").hasRole("AGENT")
                        .requestMatchers("/tickets/{ticketId}/priority").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers("/tickets/{ticketId}/category").hasAnyRole("AGENT", "ADMIN")
//                        .requestMatchers("/tickets/**").authenticated()
                        .anyRequest().authenticated()
                )
                //Disables form login
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                //Added jwtAuthFilter to the SecurityFilterChain before the UsernamePasswordAuthenticationFilter
                //Also added RateLimitFilter before jwtAuthFilter
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                //We add the oAuth2Login configuration and set its failure and success handlers ---
                //Failure handler just gives us an error log
                //Success Handler --- see in oAuth2SuccessHandler class
                //For oAuth2 we have set the autorization endpoint as /auth/oauth2/authorization --- when this endpoint will be hit then only we will login through Google
                .oauth2Login(oAuth2 -> oAuth2
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/auth/oauth2/authorization") // ✅ your custom path
                        )
                        .failureHandler(((request, response, exception) -> {
                            System.out.println("O Auth 2 Error : {}" + exception.getMessage());
                        } ))
                        .successHandler(oauth2successHandler))
                //Here in Role Based Access control we are handling the accessDenied Exception ---
                //We are sending it to the GlobalException Handlers  for the task ---
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.accessDeniedHandler((request, response, accessDeniedException) -> {
                    handlerExceptionResolver.resolveException(request,response, null , accessDeniedException);
                }))
                //We have done this to prevent oAuth2 to interfere with normal email password login
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                );

        //We add the oAuth2Login configuration and set its failure and success handlers ---
        //Failure handler just gives us an error log
        //Success Handler --- see in oAuth2SuccessHandler class

        return httpSecurity.build();
    }
}
