package com.blog_application.backend.services;

import com.blog_application.backend.config.CustomUserDetails;
import com.blog_application.backend.exceptions.BadRequestException;
import com.blog_application.backend.exceptions.ForbiddenException;
import com.blog_application.backend.models.User;
import com.blog_application.backend.repositories.UserRepository;
import com.blog_application.backend.requests.SignupRequest;
import com.blog_application.backend.responses.JwtResponse;
import com.blog_application.backend.transformers.RequestToEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;

    public JwtResponse loginUser(String email, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

            String token = jwtService.generateToken(details.getUsername());

            return JwtResponse.builder().token(token).build();
        } catch (Exception e) {
            throw new ForbiddenException();
        }
    }

    public void registerUser(SignupRequest signupRequest) {
        if(signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

            log.info("CREATING USER...");

            User user = RequestToEntity.signupRequestToUser(signupRequest);

            Optional<User> presentUser = userRepository.findByEmail(signupRequest.getEmail());

            if(presentUser.isPresent()) {
                log.error("USER ALREADY PRESENT");
                throw new BadRequestException("User already present. Try with different email");
            }

            log.info("CREATING USER IN THE DATABASE");

            userRepository.save(user);

            log.info("USER CREATED");
        } else {
            throw new BadRequestException("Passwords do not match.");
        }
    }
}
