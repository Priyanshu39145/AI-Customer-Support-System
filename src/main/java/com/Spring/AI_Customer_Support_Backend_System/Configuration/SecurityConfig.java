package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import com.Spring.AI_Customer_Support_Backend_System.Security.JWTAuthFilter;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception  {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login/**").permitAll()
                        .requestMatchers("/tickets/{ticketId}/assign").hasRole("ADMIN")
                        .requestMatchers("/tickets/{ticketId}/status").hasRole("AGENT")
                        .requestMatchers("/tickets/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                //We add the oAuth2Login configuration and set its failure and success handlers ---
                //Failure handler just gives us an error log
                //Success Handler --- see in oAuth2SuccessHandler class
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
