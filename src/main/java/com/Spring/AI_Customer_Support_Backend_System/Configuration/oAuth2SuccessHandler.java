package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import com.Spring.AI_Customer_Support_Backend_System.DTO.LoginResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
//Here we implement the AuthenticationSuccessHandler method --- which has the Authentication object along with the request responses objects
//We first cast the Authentication object into OAuth2AuthenticationToken type ----
//And then from that token --- we extract the OAuth2User using getPrinciple method
public class oAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException, RuntimeException{
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();

        //This registrationId will be used to get the provider ID --- indicates us that the user has logged in from where
        String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

        //We will handle the login using this method in AuthService ---

        //IMP Method ---
        ResponseEntity<LoginResponseDTO> loginResponse = authService.handleOAuth2loginrequest(oAuth2User,registrationId);

        //Now since we have got the LoginResponseDTO ---- we can configure the http response object to view the response
        //First we set the HTTP Status ---
        //Then we set the response type to JSON ---
        //Then we use Object Mapper to write the contents of the LoginResponseDTO as String ---

        response.setStatus(loginResponse.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse.getBody()));






    }
}
