package com.raid.blog.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.raid.blog.domain.dtos.*;
import com.raid.blog.repositories.TagRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TagControllerIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private UUID existingTagId;

    @BeforeAll
    void setup() {
        authenticate();
    }

    public void authenticate() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Raid")
                .email("thabet.raid123@gmail.com")
                .password("RaidThabet2003")
                .build();

        ResponseEntity<Void> registerResponse = restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Void.class);
        System.out.println("Register response status: " + registerResponse.getStatusCode());

        LoginRequest loginRequest = LoginRequest
                .builder()
                .email("thabet.raid123@gmail.com")
                .password("RaidThabet2003")
                .build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/v1/auth", loginRequest, AuthResponse.class);

        AuthResponse authResponse = response.getBody();
        this.jwtToken = (authResponse != null) ? authResponse.getToken() : null;

        assertNotNull(this.jwtToken);
        assertFalse(this.jwtToken.isEmpty());

    }

    @Test
    @Order(1)
    public void should_create_two_nonexisting_tags_with_valid_request() throws JsonProcessingException {
        CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                .names(Set.of("Tag 1", "Tag 2"))
                .build();

        ResponseEntity<String> response = createTags(createTagsRequest);
        createTags(createTagsRequest);

        var statusCode = response.getStatusCode();

        if (statusCode.is2xxSuccessful()) {
            assertEquals(HttpStatus.CREATED, statusCode);
            List<TagDto> createdTags = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertNotNull(createdTags);
            assertEquals(2, createdTags.size());
            assertEquals(
                    Set.of("Tag 1", "Tag 2"),
                    createdTags.stream().map(TagDto::getName).collect(Collectors.toSet())
            );
            assertEquals(2L, tagRepository.count());
            assertEquals(1L, tagRepository.countById(createdTags.getFirst().getId()));
            assertEquals(1L, tagRepository.countById(createdTags.getLast().getId()));
            this.existingTagId = createdTags.getFirst().getId();
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_not_create_new_tags_with_empty_request() throws JsonProcessingException {
        CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                .names(Set.of())
                .build();

        ResponseEntity<String> response = createTags(createTagsRequest);

        var statusCode = response.getStatusCode();

        if (statusCode.is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, statusCode);
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Validation failed", errorResponse.getMessage());
            assertNotNull(errorResponse);
            assertNotNull(errorResponse.getErrors());
            assertEquals(1, errorResponse.getErrors().size());
            assertEquals("names", errorResponse.getErrors().getFirst().getField());
            assertEquals("At least one tag name is required", errorResponse.getErrors().getFirst().getMessage());
        } else if (statusCode.is2xxSuccessful()) {
            List<TagDto> createdTags = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            fail("Should fail but created these tags: " + createdTags.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_not_create_new_tags_with_invalid_tags_names() throws JsonProcessingException {
        CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                .names(Set.of("T", "Tag ".repeat(10)))
                .build();

        ResponseEntity<String> response = createTags(createTagsRequest);

        var statusCode = response.getStatusCode();

        if (statusCode.is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, statusCode);
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Validation failed", errorResponse.getMessage());
            assertNotNull(errorResponse);
            assertNotNull(errorResponse.getErrors());
            assertEquals(2, errorResponse.getErrors().size());
            assertEquals("names[]", errorResponse.getErrors().getFirst().getField());
            assertEquals("names[]", errorResponse.getErrors().getLast().getField());
            assertEquals("Tag name must be between 2 and 30 characters", errorResponse.getErrors().getFirst().getMessage());
            assertEquals("Tag name must be between 2 and 30 characters", errorResponse.getErrors().getLast().getMessage());
        } else if (statusCode.is2xxSuccessful()) {
            List<TagDto> createdTags = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            fail("Should fail but created these tags: " + createdTags.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    public void should_not_create_new_tags_with_large_set() throws JsonProcessingException {
        CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                .names(
                        IntStream.rangeClosed(1, 15)
                                .mapToObj(i -> "New Tag " + i)
                                .collect(Collectors.toSet())
                )
                .build();

        ResponseEntity<String> response = createTags(createTagsRequest);

        var statusCode = response.getStatusCode();
        if (statusCode.is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, statusCode);
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Validation failed", errorResponse.getMessage());
            assertNotNull(errorResponse);
            assertNotNull(errorResponse.getErrors());
            assertEquals(1, errorResponse.getErrors().size());
            assertEquals("names", errorResponse.getErrors().getFirst().getField());
            assertEquals("Maximum 10 tags allowed", errorResponse.getErrors().getFirst().getMessage());
        } else if (statusCode.is2xxSuccessful()) {
            List<TagDto> createdTags = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            fail("Should fail but created these tags: " + createdTags.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(2)
    public void should_list_all_tags() throws JsonProcessingException {
        ResponseEntity<String> response = getAllTags();

        var statusCode = response.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, statusCode);
            List<TagDto> retrievedTags = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertEquals(2, retrievedTags.size());
            assertEquals(
                    Set.of("Tag 1", "Tag 2"),
                    retrievedTags.stream().map(TagDto::getName).collect(Collectors.toSet())
            );
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(3)
    public void should_delete_existing_tag() throws JsonProcessingException {
        ResponseEntity<String> response = deleteTag(this.existingTagId);

        var statusCode = response.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            assertEquals(HttpStatus.NO_CONTENT, statusCode);
            assertFalse(tagRepository.existsById(this.existingTagId));
            assertEquals(1L, tagRepository.count());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    private ResponseEntity<String> createTags(CreateTagsRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateTagsRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/tags",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            String prettyJson = writer.writeValueAsString(objectMapper.readTree(response.getBody()));

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

    private ResponseEntity<String> getAllTags() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.jwtToken);

        HttpEntity<TagDto> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/tags",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            String prettyJson = writer.writeValueAsString(objectMapper.readTree(response.getBody()));

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

    private ResponseEntity<String> deleteTag(UUID tagId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.jwtToken);

        HttpEntity<TagDto> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/tags/" + tagId,
                    HttpMethod.DELETE,
                    httpEntity,
                    String.class
            );

            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());

            return response;
        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
