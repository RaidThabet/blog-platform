package com.raid.blog.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.raid.blog.domain.dtos.ApiErrorResponse;
import com.raid.blog.domain.dtos.AuthResponse;
import com.raid.blog.domain.dtos.LoginRequest;
import com.raid.blog.domain.dtos.RegisterRequest;
import com.raid.blog.domain.entities.User;
import com.raid.blog.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClientException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public void setup() {
        String encodedPassword = passwordEncoder.encode("password");
        User existingUser = User.builder()
                .name("Raid")
                .email("raid@mail.com")
                .password(encodedPassword)
                .build();

        userRepository.save(existingUser);
    }

    @Test
    public void should_authenticate_existing_user_with_valid_credentials() throws JsonProcessingException {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("raid@mail.com")
                .password("password")
                .build();

        ResponseEntity<String> response = sendPostRequest(loginRequest, "/api/v1/auth");

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            AuthResponse authResponse = objectMapper.readValue(response.getBody(), AuthResponse.class);
            assertNotNull(authResponse);
            assertNotNull(authResponse.getToken());
            assertTrue(authResponse.getExpiresIn() > 0);
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_return_an_error_when_authenticating_with_empty_request_body() throws JsonProcessingException {
        ResponseEntity<String> response = sendPostRequest("", "/api/v1/auth");

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(errorResponse);
            assertNull(errorResponse.getErrors());
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Required request body is missing", errorResponse.getMessage());
        } else if (response.getStatusCode().is2xxSuccessful()) {
            AuthResponse authResponse = objectMapper.readValue(response.getBody(), AuthResponse.class);
            fail("User successfully logged in and returned object is: " + authResponse.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_return_an_error_when_authenticating_with_invalid_credentials() throws JsonProcessingException {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("raid")
                .password("pa")
                .build();

        ResponseEntity<String> response = sendPostRequest(loginRequest, "/api/v1/auth");

        if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(errorResponse);
            assertNotNull(errorResponse.getErrors());
            assertEquals("Validation failed", errorResponse.getMessage());
            assertEquals(
                    Set.of("email", "password"),
                    errorResponse.getErrors().stream().map(ApiErrorResponse.FieldError::getField).collect(Collectors.toSet())
            );
        } else if (response.getStatusCode().is2xxSuccessful()) {
            fail("Invalid login request was treated successfully: " + loginRequest.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_not_authenticate_a_non_existing_user() throws JsonProcessingException {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("fake@mail.com")
                .password("wrong_password")
                .build();

        ResponseEntity<String> response = sendPostRequest(loginRequest, "/api/v1/auth");

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(errorResponse);
            assertNull(errorResponse.getErrors());
            assertEquals(401, errorResponse.getStatus());
            assertEquals("Incorrect username or password", errorResponse.getMessage());
        } else if (response.getStatusCode().is2xxSuccessful()) {
            AuthResponse authResponse = objectMapper.readValue(response.getBody(), AuthResponse.class);
            fail("User successfully logged in and returned object is: " + authResponse.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_register_a_new_user() throws JsonProcessingException {
        RegisterRequest request = RegisterRequest.builder()
                .name("Raid")
                .email("raid2@mail.com")
                .password("password")
                .build();

        ResponseEntity<String> response = sendPostRequest(request, "/api/v1/auth/register");

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            assertTrue(userRepository.existsByEmail("raid2@mail.com"));
            assertEquals(2, userRepository.count());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_not_create_new_user_with_invalid_request() throws JsonProcessingException {
        RegisterRequest request = RegisterRequest.builder()
                .build();

        ResponseEntity<String> response = sendPostRequest(request, "/api/v1/auth/register");

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ApiErrorResponse apiErrorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(apiErrorResponse);
            assertNotNull(apiErrorResponse.getErrors());
            assertEquals("Validation failed", apiErrorResponse.getMessage());
            assertEquals(
                    Set.of("email", "password", "name"),
                    apiErrorResponse.getErrors().stream().map(ApiErrorResponse.FieldError::getField).collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void should_not_create_new_user_with_empty_request() throws JsonProcessingException {
        ResponseEntity<String> response = sendPostRequest("", "/api/v1/auth/register");

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(errorResponse);
            assertNull(errorResponse.getErrors());
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Required request body is missing", errorResponse.getMessage());
        } else if (response.getStatusCode().is2xxSuccessful()) {
            AuthResponse authResponse = objectMapper.readValue(response.getBody(), AuthResponse.class);
            fail("User successfully logged in and returned object is: " + authResponse.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_not_register_an_existing_user() throws JsonProcessingException {
        RegisterRequest request = RegisterRequest.builder()
                .name("Raid 2")
                .email("raid@mail.com")
                .password("password")
                .build();

        ResponseEntity<String> response = sendPostRequest(request, "/api/v1/auth/register");

        if (response.getStatusCode().equals(HttpStatus.CONFLICT)) {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(errorResponse);
            assertNull(errorResponse.getErrors());
            assertEquals("User with this email already exists", errorResponse.getMessage());
        } else if (response.getStatusCode().is2xxSuccessful()) {
            fail("User successfully created again");
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_reject_login_with_sql_injection_attempt() throws JsonProcessingException {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("'; DROP TABLE users; --")
                .password("password")
                .build();

        ResponseEntity<String> response = sendPostRequest(loginRequest, "/api/v1/auth");

        if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertEquals("Validation failed", errorResponse.getMessage());
            assertEquals("email", errorResponse.getErrors().getFirst().getField());
            assertEquals("Email is invalid", errorResponse.getErrors().getFirst().getMessage());
        } else if (response.getStatusCode().is2xxSuccessful()) {
            fail("User successfully created again");
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    private ResponseEntity<String> sendPostRequest(Object request, String endpoint) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestHttpEntity = new HttpEntity<>(request, httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestHttpEntity,
                    String.class
            );

            String requestJSON = objectMapper.writeValueAsString(request);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            String prettyJson;
            if (!response.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                prettyJson = writer.writeValueAsString(objectMapper.readTree(response.getBody()));
            } else {
                prettyJson = "";
            }
            String prettyJsonRequest = writer.writeValueAsString(objectMapper.readTree(requestJSON));

            System.out.println("Raw JSON request: \n" + prettyJsonRequest);
            System.out.println("Raw JSON response: \n" + prettyJson);
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());

            return response;
        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

