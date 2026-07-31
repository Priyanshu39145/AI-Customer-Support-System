package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import com.Spring.AI_Customer_Support_Backend_System.DTO.LoginResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Security.AuthenticationCookieService;
import com.Spring.AI_Customer_Support_Backend_System.Services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class oAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final AuthenticationCookieService authenticationCookieService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:5174}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, RuntimeException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();

        String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

        ResponseEntity<LoginResponseDTO> loginResponse = authService.handleOAuth2loginrequest(oAuth2User, registrationId);
        LoginResponseDTO loginResponseDTO = loginResponse.getBody();

        authenticationCookieService.setAuthenticationCookies(
                response, loginResponseDTO.getAccessToken(), loginResponseDTO.getRefreshToken());
        response.sendRedirect(frontendUrl + "/oauth2/callback");
    }
}
//Done