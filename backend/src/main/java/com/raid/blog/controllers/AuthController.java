package com.raid.blog.controllers;

import com.raid.blog.domain.dtos.AuthResponse;
import com.raid.blog.domain.dtos.LoginRequest;
import com.raid.blog.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        UserDetails userDetails = authenticationService.authenticate(request.getEmail(), request.getPassword());
        String tokenValue = authenticationService.generateToken(userDetails);

        var authResponse = AuthResponse.builder()
                .token(tokenValue)
                .expiresIn(86400)
                .build();

        return ResponseEntity.ok(authResponse);
    }
}
