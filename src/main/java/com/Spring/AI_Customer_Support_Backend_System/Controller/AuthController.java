package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.DTO.*;
import com.Spring.AI_Customer_Support_Backend_System.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //From here user registers using Email and password and name --- RegisterRequestDTO -----
    //see the register method is AuthService ----
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequestDTO));
    }
    //This is the login endpoint --- it requires the LoginRequestDTO --- which requires email and password ---
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO)    {
        return ResponseEntity.ok(authService.login(loginRequestDTO));
    }

    //This endpoint is used for refreshing the JWT token periodically before it expires ---
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.refresh(requestDTO));
    }

    //This is for logout of the users ---- we require the refreshToken for it ----
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDTO requestDTO) {
        authService.logout(requestDTO);
        return ResponseEntity.noContent().build();
    }

}
