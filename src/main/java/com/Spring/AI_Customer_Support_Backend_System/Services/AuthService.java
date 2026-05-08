package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.LoginRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.LoginResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.RegisterRequestDTO;
import com.Spring.AI_Customer_Support_Backend_System.DTO.RegisterResponseDTO;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.ProviderType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import com.Spring.AI_Customer_Support_Backend_System.Security.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;

    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        //We first check for the existence of the user in the database --- if it is present then we give an exception ---
        log.info("Register attempt for email: {}", registerRequestDTO.getEmail());
        User user = userRepository.findByEmail(registerRequestDTO.getEmail()).orElse(null);

        if(user!=null)  {
            log.warn("Registration failed - user already exists: {}", registerRequestDTO.getEmail());
            throw new IllegalArgumentException("User already exists");
        }


        //Then we create a new user ---- inside the DB --- and then return the response ----
        User newuser = User.builder()
                .email(registerRequestDTO.getEmail())
                .name(registerRequestDTO.getName())
                .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .role(RoleType.USER)
                .enabled(true)
                .providerType(ProviderType.EMAIL)
                .build();

        userRepository.save(newuser);
        log.info("User registered successfully: {}", newuser.getEmail());
        return modelMapper.map(newuser, RegisterResponseDTO.class);
    }


    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        log.info("Login attempt for email: {}", loginRequestDTO.getEmail());
        //We first authenticate using the authentication manager which internally uses the UsernamePasswordAuthenticationToken containing the username and password for the user ----
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail() , loginRequestDTO.getPassword())
        );

        //We get the user object from the authentication object once its authenticated from the database ---
        //How it authenticates --- see the CustomUserDetailsService ---- it has the method loadUser --- which checks the incoming user by the email --- and thus it finds the user is present in the DB or not ---
        //That method returns the user inside the authentication object ------
        //See the User entity --- it implements the UserDetails interface --- it helps us to link the User entity to the Spring Security such that the authenticationManager always checks the userRepository for the user ---
        User user = (User) authentication.getPrincipal();


        //We create the JWT token using the user object here ----
        String token = authUtil.generateAccessToken(user);
        log.info("Login successful for user: {}", user.getEmail());
        //We return the response including the JWT token ---
        return new LoginResponseDTO(user.getEmail() , user.getRole() , token);
    }

    @Transactional
    public ResponseEntity<LoginResponseDTO> handleOAuth2loginrequest(OAuth2User oAuth2User, String registrationId) {
        log.info("OAuth2 login attempt via provider: {}", registrationId);
        //Remember first set AuthProviderType and providerId in User Entity ---
        //First fetch provider Type and provider ID --- the method --- to get the AuthProviderType and providerId from registration Id is in AuthUTIL --- refer there
        ProviderType authProviderType = authUtil.getAuthProviderTypeFromRegistrationID(registrationId);
        String providerId = authUtil.getProviderIdFromOAuthUser(oAuth2User,registrationId);

        //We can also get the name of the USer from the attributes ---
        String name = oAuth2User.getAttribute("name");
        log.debug("OAuth2 providerId: {}", providerId);

        //First we have to find the User using the ProviderId and ProviderType ----
        User user1 = (User) userRepository.findByProviderIdAndProviderType(providerId,authProviderType).orElse(null);
        //Now we know that in our previous lectures we have made the user log in through manually by giving their email and password --- Now if the user with the same email id tries to log in --- we should not make another account in our database ----
        //oAuth --- gives us option to fetch the email of the user ---
        String email = oAuth2User.getAttribute("email");
        if(email == null || email.isBlank()) {
            log.error("OAuth2 login failed - email not provided");
            throw new IllegalArgumentException("Email not provided by OAuth provider");
        }
        //Using this email we will fetch the user from the database ---- inside the database we are usually storing email inside the username attribute ----- however if we dont get email --- then we store other things --- consider email is store inside the username attribute ---
        User user2 = userRepository.findByEmail(email).orElse(null);

        //Now if both the user1 and user2 is null then we are sure that the user is not in the database and we need to signup that user
        if(user1==null && user2==null)  {
            log.info("OAuth2 new user detected. Registering: {}", email);
            //We use the signupReturningUser method which returns a User(Conventional signup method gives us the SignUpResponseDTO --- so to get the user object from it we make another method here which returns the User directly) --- and requires an LoginRequestDTO consisting of username and password --- password is set to null ---- as oAuth2 doesnt require passwords ---
            //Now since there are two more ---- attributes in User --- providerId and providerType --- so we have to also send them to the signup method to set to the User
            //During addition of Role Based access --- since we are associating the User with Patient --- we need a SignupRequestDTO ---- containing the Patient not nullable info too ---- so we configure SignUpRequestDTO here instead of LoginRequestDTO
            user1 = registerByOAuth2(name,email,providerId,authProviderType);
        }
        //If we have a user from the Provider ---
        //And we have also received the email ---
        //But the email is not equal to the username of the user ---- then we know that the username doesnt have the email ---
        //See the above findUserNameFromoAuthUser code ---- There we have saved the username with different fields if we didnt get the email ---- So we need to set the username with email here ---
        else if(user1!=null)    {
            if(email!=null && !email.isBlank() && !email.equals(user1.getUsername()))   {
                log.info("Updating email for OAuth2 user: {}", email);
                user1.setEmail(email);
                userRepository.save(user1);
            }
        }
        //If the user exists with the email and also the email is equal to the username ---
        //Then we need not create another user --- we throw an Exception
        //This is not needed as we need to log in the user then --- and we do it in the next step ---
//        else {
//            throw new BadCredentialsException("This email is already registered with the provider");
//        }
        log.info("OAuth2 login successful for user: {}", email);
        //Now we have to log in the user by sending a LoginResponseDTO ---- It requires the JWT and the userId
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(user1.getEmail(), user1.getRole(), authUtil.generateAccessToken(user1));

        return ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);

        //So what do we do here --- we first fetch the providerId and providerType ---
        //Then using a Business Logic try to save the user inside the database including the Provider Id and ProviderType ---
        //Then we log in returning the login Response DTO which has the JWT token ----


        //Save the provider ID and provider Type along with the USer ---- we save the proviuder details along with the user to know from where the user has logged in ---- if any other day the user has logged in with Github then we should not make another account for different user --- we should have the same user ---
        //To save the provider Type and provider ID --- we have to change the User Entity --- see the changes in the USer Entity


        //If the user has an account directly log in

        //If the user doesnt have an account  --- first signup then log in
    }

    private User registerByOAuth2(String name, String email, String providerId, ProviderType authProviderType) {
        log.debug("Registering OAuth2 user: {}", email);
        User user = userRepository.findByEmail(email).orElse(null);
        if(user!=null) {
            log.warn("OAuth2 registration failed - user already exists: {}", email);
            throw new IllegalArgumentException("User already exists");
        }

        User newUser = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(RoleType.USER)
                .enabled(true)
                .providerId(providerId)
                .providerType(authProviderType)
                .build();

        userRepository.save(newUser);
        log.info("OAuth2 user registered successfully: {}", email);
        return newUser;

    }
}
