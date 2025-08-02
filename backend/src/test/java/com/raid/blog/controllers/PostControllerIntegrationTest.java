package com.raid.blog.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raid.blog.domain.PostStatus;
import com.raid.blog.domain.dtos.*;
import com.raid.blog.domain.entities.Category;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.services.CategoryService;
import com.raid.blog.services.TagService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostControllerIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private Category category1;

    private Category category2;

    private List<Tag> tags = new ArrayList<>();

    private UUID postId;

    @BeforeAll
    void setup() {
        setupCategoriesAndTags();
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

    public void setupCategoriesAndTags() {
        Category category1 = Category.builder()
                .name("Category 1")
                .build();
        this.category1 = categoryService.createCategory(category1);

        Category category2 = Category.builder()
                .name("Category 2")
                .build();
        this.category2 = categoryService.createCategory(category2);

        this.tags = tagService.createTags(Set.of("Tag 1", "Tag 2"));
    }

    @Test
    @Order(value = 1)
    public void should_create_three_posts() throws JsonProcessingException {
        CreatePostRequestDto createPostRequestDto1 = CreatePostRequestDto.builder()
                .title("Title 1")
                .content("This is some testing content text")
                .status(PostStatus.PUBLISHED)
                .tagIds(Set.of(this.tags.getFirst().getId()))
                .categoryId(this.category1.getId())
                .build();

        CreatePostRequestDto createPostRequestDto2 = CreatePostRequestDto.builder()
                .title("Title 2")
                .content("This is some testing content text")
                .status(PostStatus.PUBLISHED)
                .tagIds(Set.of(this.tags.getLast().getId()))
                .categoryId(this.category2.getId())
                .build();

        CreatePostRequestDto createPostRequestDto3 = CreatePostRequestDto.builder()
                .title("Title 3")
                .content("This is some testing content text")
                .status(PostStatus.DRAFT)
                .tagIds(Set.of())
                .categoryId(this.category2.getId())
                .build();

        ResponseEntity<String> response1 = createPost(createPostRequestDto1);
        if (response1.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.CREATED, response1.getStatusCode());
            PostDto createdPost1 = objectMapper.readValue(response1.getBody(), PostDto.class);
            assertNotNull(createdPost1);
            assertEquals("Title 1", createdPost1.getTitle());
            assertEquals("This is some testing content text", createdPost1.getContent());
            assertEquals(this.category1.getId(), createdPost1.getCategory().getId());
            assertEquals(
                    Set.of(this.tags.stream().map(Tag::getId).toList().getFirst()),
                    Set.of(createdPost1.getTags().stream().map(TagDto::getId).toList().getFirst())
            );
            this.postId = createdPost1.getId();
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response1.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }


        ResponseEntity<String> response2 = createPost(createPostRequestDto2);
        if (response2.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.CREATED, response2.getStatusCode());
            PostDto createdPost2 = objectMapper.readValue(response2.getBody(), PostDto.class);
            assertNotNull(createdPost2);
            assertEquals("Title 2", createdPost2.getTitle());
            assertEquals("This is some testing content text", createdPost2.getContent());
            assertEquals(this.category2.getId(), createdPost2.getCategory().getId());
            assertEquals(
                    Set.of(this.tags.stream().map(Tag::getId).toList().getLast()),
                    Set.of(createdPost2.getTags().stream().map(TagDto::getId).toList().getLast())
            );
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response1.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }

        ResponseEntity<String> response3 = createPost(createPostRequestDto3);
        if (response1.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.CREATED, response3.getStatusCode());
            PostDto createdPost3 = objectMapper.readValue(response3.getBody(), PostDto.class);
            assertNotNull(createdPost3);
            assertEquals("Title 3", createdPost3.getTitle());
            assertEquals("This is some testing content text", createdPost3.getContent());
            assertEquals(this.category2.getId(), createdPost3.getCategory().getId());
            assertEquals(0, createdPost3.getTags().size());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response1.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 2)
    public void should_not_create_post_with_invalid_request() throws JsonProcessingException {
        CreatePostRequestDto createPostRequestDto = CreatePostRequestDto.builder()
                .title("")
                .content("")
                .categoryId(this.category1.getId())
                .tagIds(Set.of())
                .status(PostStatus.DRAFT)
                .build();
        ResponseEntity<String> response = createPost(createPostRequestDto);

        System.out.println(response.getBody());

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Validation failed", errorResponse.getMessage());
            assertNotNull(errorResponse.getErrors());
            assertEquals(4, errorResponse.getErrors().size());
            assertEquals(Set.of("title", "content"), errorResponse.getErrors().stream().map(ApiErrorResponse.FieldError::getField).collect(Collectors.toSet()));
        } else if (response.getStatusCode().is2xxSuccessful()) {
            PostDto createdPost = objectMapper.readValue(response.getBody(), PostDto.class);
            fail("Post with bad creation request was successfully created: " + createdPost.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 3)
    public void should_list_all_created_posts() throws JsonProcessingException {
        ResponseEntity<String> response = getListOfPosts("");

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<PostDto> posts = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertNotNull(posts);
            assertEquals(2, posts.size());
            assertEquals("Title 1", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals("Title 2", posts.getLast().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getLast().getStatus());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 4)
    public void should_list_posts_with_specific_category() throws JsonProcessingException {
        ResponseEntity<String> response = getListOfPosts("?categoryId=" + this.category1.getId());

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<PostDto> posts = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertNotNull(posts);
            assertEquals(1, posts.size());
            assertEquals("Title 1", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals(this.category1.getId(), posts.getFirst().getCategory().getId());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 5)
    public void should_list_posts_with_specific_tag() throws JsonProcessingException {
        ResponseEntity<String> response = getListOfPosts("?tagId=" + this.tags.getFirst().getId());

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<PostDto> posts = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertNotNull(posts);
            assertEquals(1, posts.size());
            assertEquals("Title 1", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals(this.tags.getFirst().getId(), posts.getFirst().getTags().stream().toList().getFirst().getId());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }

    }

    @Test
    @Order(value = 6)
    public void should_list_posts_with_specific_tag_and_category() throws JsonProcessingException {
        ResponseEntity<String> response = getListOfPosts("?tagId=" + this.tags.getLast().getId() + "&categoryId=" + this.category2.getId());

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<PostDto> posts = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertNotNull(posts);
            assertEquals(1, posts.size());
            assertEquals("Title 2", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals(this.tags.getLast().getId(), posts.getFirst().getTags().stream().toList().getFirst().getId());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 7)
    public void should_return_error_when_listing_posts_with_nonexistent_category() throws JsonProcessingException {
        ResponseEntity<String> response = getListOfPosts("?categoryId=" + UUID.randomUUID());

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            System.out.println(errorResponse);
            assertNotNull(errorResponse);
            assertEquals(404, errorResponse.getStatus());
            assertEquals("Category not found", errorResponse.getMessage());
            assertNull(errorResponse.getErrors());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 8)
    public void should_get_existing_post_by_id() throws JsonProcessingException {
        ResponseEntity<String> response = getPostByItsId();

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PostDto retrievedPost = objectMapper.readValue(response.getBody(), PostDto.class);
            assertNotNull(retrievedPost);
            assertEquals(retrievedPost.getId(), this.postId);
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 9)
    public void should_update_existing_post_with_valid_request() throws JsonProcessingException {
        UpdatePostRequestDto updatePostRequestDto = UpdatePostRequestDto.builder()
                .id(this.postId) // id of post 1
                .title("New Title 1")
                .content("New content for post 1")
                .categoryId(this.category2.getId())
                .tagIds(Set.of())
                .status(PostStatus.DRAFT)
                .build();
        ResponseEntity<String> response = updatePost(updatePostRequestDto, this.postId);

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PostDto updatedPost = objectMapper.readValue(response.getBody(), PostDto.class);
            assertNotNull(updatedPost);
            assertEquals(updatedPost.getId(), this.postId);
            assertEquals("New Title 1", updatedPost.getTitle());
            assertEquals("New content for post 1", updatedPost.getContent());
            assertEquals(PostStatus.DRAFT, updatedPost.getStatus());
            assertEquals(0, updatedPost.getTags().size());
            assertEquals(this.category2.getId(), updatedPost.getCategory().getId());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 10)
    public void should_not_update_nonexisting_post() throws JsonProcessingException {
        UUID randomPostId = UUID.randomUUID();
        UpdatePostRequestDto updatePostRequestDto = UpdatePostRequestDto.builder()
                .id(randomPostId)
                .title("New Title 1")
                .content("New content for post 1")
                .categoryId(this.category2.getId())
                .tagIds(Set.of())
                .status(PostStatus.DRAFT)
                .build();
        ResponseEntity<String> response = updatePost(updatePostRequestDto, randomPostId);

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(errorResponse);
            assertEquals(404, errorResponse.getStatus());
            assertEquals("Post does not exist", errorResponse.getMessage());
            assertNull(errorResponse.getErrors());
        } else if (response.getStatusCode().is2xxSuccessful()) {
            PostDto updatedPost = objectMapper.readValue(response.getBody(), PostDto.class);
            fail("Non existent post got updated: " + updatedPost.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 11)
    public void should_not_update_existing_post_with_invalid_request() throws JsonProcessingException {
        UpdatePostRequestDto updatePostRequestDto = UpdatePostRequestDto.builder()
                .build();
        ResponseEntity<String> response = updatePost(updatePostRequestDto, this.postId);

        if (response.getStatusCode().is4xxClientError()) {
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            assertNotNull(errorResponse);
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Validation failed", errorResponse.getMessage());
            assertNotNull(errorResponse.getErrors());
            assertEquals(5, errorResponse.getErrors().size());
            assertEquals(
                    Set.of("id", "title", "content", "categoryId", "status"),
                    errorResponse.getErrors().stream().map(ApiErrorResponse.FieldError::getField).collect(Collectors.toSet())
            );
        } else if (response.getStatusCode().is2xxSuccessful()) {
            PostDto updatedPost = objectMapper.readValue(response.getBody(), PostDto.class);
            fail("Non existent post got updated: " + updatedPost.toString());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    @Test
    @Order(value = 12)
    public void should_delete_existing_post() throws JsonProcessingException {
        ResponseEntity<String> response = deletePost(this.postId);

        if (response.getStatusCode().is2xxSuccessful()) {
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        } else {
            ApiErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            fail(errorResponse.toString());
        }
    }

    private ResponseEntity<String> createPost(CreatePostRequestDto createPostRequestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreatePostRequestDto> requestDtoHttpEntity = new HttpEntity<>(createPostRequestDto, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/posts",
                    HttpMethod.POST,
                    requestDtoHttpEntity,
                    String.class
            );

            System.out.println("Raw JSON response: " + response.getBody());
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());

            return response;
        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseEntity<String> getListOfPosts(String requestQueryParams) {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<PostDto> requestEntity = new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/posts" + requestQueryParams,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            System.out.println("Raw JSON response: " + response.getBody());
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());

            return response;
        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseEntity<String> getPostByItsId() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(this.jwtToken);
        HttpEntity<PostDto> requestEntity = new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/posts/" + this.postId,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            System.out.println("Raw JSON response: " + response.getBody());
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());

            return response;

        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseEntity<String> updatePost(UpdatePostRequestDto requestDto, UUID postId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(this.jwtToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdatePostRequestDto> requestEntity = new HttpEntity<>(requestDto, httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/posts/" + postId,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            System.out.println("Raw JSON response: " + response.getBody());
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());

            return response;

        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseEntity<String> deletePost(UUID postId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(this.jwtToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdatePostRequestDto> requestEntity = new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/posts/" + postId,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            System.out.println("Raw JSON response: " + response.getBody());
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
