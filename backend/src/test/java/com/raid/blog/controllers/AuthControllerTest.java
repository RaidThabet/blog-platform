package com.raid.blog.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.raid.blog.config.TestSecurityConfig;
import com.raid.blog.domain.dtos.ApiErrorResponse;
import com.raid.blog.domain.dtos.AuthResponse;
import com.raid.blog.domain.dtos.LoginRequest;
import com.raid.blog.domain.dtos.RegisterRequest;
import com.raid.blog.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    public void should_return_an_error_when_logging_in_with_request_body_is_missing() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();
        String requestBOdy = objectMapper.writeValueAsString(loginRequest);
        UserDetails mockUser = mock(UserDetails.class);

        // What to return
        given(authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())).willReturn(mockUser);
        given(authenticationService.generateToken(mockUser)).willReturn("mock-jwt-token");

        // Act and assert
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/auth")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(objectMapper.readTree(responseJson));
        System.out.println("Raw JSON response: \n" + prettyJson);
        ApiErrorResponse errorResponse = objectMapper.readValue(responseJson, ApiErrorResponse.class);

        assertNotNull(errorResponse);
        assertNull(errorResponse.getErrors());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Required request body is missing", errorResponse.getMessage());
    }

    @Test
    public void should_return_an_error_when_logging_in_with_invalid_request_body() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();
        String requestBOdy = objectMapper.writeValueAsString(loginRequest);
        UserDetails mockUser = mock(UserDetails.class);

        // What to return
        given(authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())).willReturn(mockUser);
        given(authenticationService.generateToken(mockUser)).willReturn("mock-jwt-token");

        // Act and assert
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/auth")
                                .contentType("application/json")
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(objectMapper.readTree(responseJson));
        System.out.println("Raw JSON response: \n" + prettyJson);
        ApiErrorResponse errorResponse = objectMapper.readValue(responseJson, ApiErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getErrors());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Validation failed", errorResponse.getMessage());
    }

    @Test
    public void should_authenticate_existing_user_with_valid_credentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();
        String requestBOdy = objectMapper.writeValueAsString(loginRequest);
        UserDetails mockUser = mock(UserDetails.class);

        // What to return
        given(authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())).willReturn(mockUser);
        given(authenticationService.generateToken(mockUser)).willReturn("mock-jwt-token");

        // Act and expect
        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/auth")
                        .contentType("application/json")
                        .content(requestBOdy)
        )
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(objectMapper.readTree(responseJson));
        System.out.println("Raw JSON response: \n" + prettyJson);
        AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);

        assertNotNull(authResponse);
        assertEquals(86400, authResponse.getExpiresIn());
        assertEquals("mock-jwt-token", authResponse.getToken());
    }

    @Test
    public void should_return_an_error_when_user_does_not_exist() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid@example.com")
                .password("random_password")
                .build();
        String requestBOdy = objectMapper.writeValueAsString(loginRequest);

        // What to return
        given(authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword()))
                .willThrow(new BadCredentialsException("Invalid credentials"));

        // Act and assert
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/auth")
                                .contentType("application/json")
                                .content(requestBOdy)
                )
                .andExpect(status().isUnauthorized())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(objectMapper.readTree(responseJson));
        System.out.println("Raw JSON response: \n" + prettyJson);
        ApiErrorResponse errorResponse = objectMapper.readValue(responseJson, ApiErrorResponse.class);

        assertNotNull(errorResponse);
        assertNull(errorResponse.getErrors());
        assertEquals(401, errorResponse.getStatus());
        assertEquals("Incorrect username or password", errorResponse.getMessage());
    }

    @Test
    public void should_register_new_user_with_valid_request() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .name("Raid")
                .email("raid@mail.com")
                .password("raid2003")
                .build();
        String requestJSON = objectMapper.writeValueAsString(request);

        // What to return

        // Act and assert
        mockMvc
                .perform(
                        post("/api/v1/auth/register")
                                .contentType("application/json")
                                .content(requestJSON)
                )
                .andExpect(status().isAccepted());
    }

    @Test
    public void should_not_register_an_existing_user() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .name("Raid")
                .email("raid@mail.com")
                .password("raid2003")
                .build();
        String requestJSON = objectMapper.writeValueAsString(request);

        // What to return
        doThrow(new IllegalStateException("User with this email already exists"))
                .when(authenticationService)
                .register(request.getName(), request.getEmail(), request.getPassword());

        // Act and assert
        MvcResult mvcResult = mockMvc
                .perform(
                        post("/api/v1/auth/register")
                                .contentType("application/json")
                                .content(requestJSON)
                )
                .andExpect(status().isConflict())
                .andReturn();

        String responseJson = mvcResult.getResponse().getContentAsString();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(objectMapper.readTree(responseJson));
        System.out.println("Raw JSON response: \n" + prettyJson);
        ApiErrorResponse errorResponse = objectMapper.readValue(responseJson, ApiErrorResponse.class);
        assertNotNull(errorResponse);
        assertNull(errorResponse.getErrors());
        assertEquals(409, errorResponse.getStatus());
        assertEquals("User with this email already exists", errorResponse.getMessage());
    }
}