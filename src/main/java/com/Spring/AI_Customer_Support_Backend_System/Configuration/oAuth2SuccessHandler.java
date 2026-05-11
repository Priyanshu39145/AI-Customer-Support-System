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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class oAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, RuntimeException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();

        String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

        ResponseEntity<LoginResponseDTO> loginResponse = authService.handleOAuth2loginrequest(oAuth2User, registrationId);
        LoginResponseDTO loginResponseDTO = loginResponse.getBody();

        // Redirect to frontend callback page with tokens in URL query params
        // Frontend OAuth2CallbackPage will extract tokens, store in localStorage, and redirect to dashboard
        String frontendUrl = "http://localhost:5174/oauth2/callback";
        StringBuilder redirectUrl = new StringBuilder(frontendUrl);
        redirectUrl.append("?accessToken=").append(URLEncoder.encode(loginResponseDTO.getAccessToken(), StandardCharsets.UTF_8));
        redirectUrl.append("&refreshToken=").append(URLEncoder.encode(loginResponseDTO.getRefreshToken(), StandardCharsets.UTF_8));

        // Add user data as encoded JSON
        String userJson = objectMapper.writeValueAsString(loginResponseDTO.getUser());
        redirectUrl.append("&user=").append(URLEncoder.encode(userJson, StandardCharsets.UTF_8));

        response.sendRedirect(redirectUrl.toString());
    }
}