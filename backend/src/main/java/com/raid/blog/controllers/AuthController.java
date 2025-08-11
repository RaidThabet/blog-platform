package com.raid.blog.controllers;

import com.raid.blog.domain.dtos.AuthResponse;
import com.raid.blog.domain.dtos.LoginRequest;
import com.raid.blog.domain.dtos.RegisterRequest;
import com.raid.blog.openapi.annotations.auth.SwaggerAuthenticateResponses;
import com.raid.blog.openapi.annotations.auth.SwaggerRegisterResponses;
import com.raid.blog.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Describes the different endpoints related to authentication")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Authenticate user", description = "Authenticate a user to his account if it exists")
    @SwaggerAuthenticateResponses
    @PostMapping
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        UserDetails userDetails = authenticationService.authenticate(request.getEmail(), request.getPassword());
        String tokenValue = authenticationService.generateToken(userDetails);

        var authResponse = AuthResponse.builder()
                .token(tokenValue)
                .expiresIn(86400)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Register user", description = "Create a new account for a user")
    @SwaggerRegisterResponses
    @PostMapping("register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        authenticationService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.accepted().build();
    }
}
