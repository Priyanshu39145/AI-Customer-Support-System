package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.*;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Security.AuthenticationCookieService;
import com.Spring.AI_Customer_Support_Backend_System.Services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/auth", "/api/auth"}) //Meaning we allow both /auth/.... or /api/auth/....
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthenticationCookieService authenticationCookieService;

    //From here user registers using Email and password and name --- RegisterRequestDTO -----
    //see the register method is AuthService ----
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("REGISTER CONTROLLER HIT");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequestDTO));
    }
    //This is the login endpoint --- it requires the LoginRequestDTO --- which requires email and password ---
    //What we change is that on login ---- we set the cookie from the login response access tokens given
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
                                                  HttpServletResponse response) {
        LoginResponseDTO loginResponse = authService.login(loginRequestDTO);
        authenticationCookieService.setAuthenticationCookies(
                response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    //This endpoint is used for refreshing the JWT token periodically before it expires ----
    //HEre the controller first reads the refresh token from the cookies ---- using CookieValue annotation
    //Then it generates new access token for the authenticated user ----
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(value = AuthenticationCookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            authenticationCookieService.clearAuthenticationCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            LoginResponseDTO loginResponse = authService.refresh(refreshToken);
            authenticationCookieService.setAuthenticationCookies(
                    response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException exception) {
            authenticationCookieService.clearAuthenticationCookies(response);
            throw exception;
        }
    }

    //This is for logout of the users ---- we require the refreshToken for it ----
    //We read the refresh token from the Cookies ----
    //Then we call the logout method inside the authService and also clear the cookies of the tokens
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = AuthenticationCookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        authenticationCookieService.clearAuthenticationCookies(response);
        return ResponseEntity.noContent().build();
    }

    //This endpoint returns the current user from the SecurityContextHolder ----- so that we can show the dashboard contents successfully
    //Previously we had stored the user details inside the localStorage ----
    //Now since we store httpOnly cookies ---- we dont store the user info there ---- instead we fetch it from this endpoint
    @GetMapping("/me")
    public ResponseEntity<LoginResponseDTO.UserDTO> me(@AuthenticationPrincipal User user) {
        LoginResponseDTO.UserDTO userDTO = LoginResponseDTO.UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
        return ResponseEntity.ok(userDTO);
    }
    //This generates csrf token ---- Frontend calls this once. Spring returns: Set-Cookie:XSRF-TOKEN
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

}
