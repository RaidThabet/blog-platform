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
    public void should_create_three_posts() {
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

        PostDto createdPost1 = createPost(createPostRequestDto1);
        PostDto createdPost2 = createPost(createPostRequestDto2);
        PostDto createdPost3 = createPost(createPostRequestDto3);

        assertNotNull(createdPost1);
        assertEquals("Title 1", createdPost1.getTitle());
        assertEquals("This is some testing content text", createdPost1.getContent());
        assertEquals(this.category1.getId(), createdPost1.getCategory().getId());
        assertEquals(
                Set.of(this.tags.stream().map(Tag::getId).toList().getFirst()),
                Set.of(createdPost1.getTags().stream().map(TagDto::getId).toList().getFirst())
        );

        assertNotNull(createdPost2);
        assertEquals("Title 2", createdPost2.getTitle());
        assertEquals("This is some testing content text", createdPost2.getContent());
        assertEquals(this.category2.getId(), createdPost2.getCategory().getId());
        assertEquals(
                Set.of(this.tags.stream().map(Tag::getId).toList().getLast()),
                Set.of(createdPost2.getTags().stream().map(TagDto::getId).toList().getLast())
        );

        assertNotNull(createdPost3);
        assertEquals("Title 3", createdPost3.getTitle());
        assertEquals("This is some testing content text", createdPost2.getContent());
        assertEquals(this.category2.getId(), createdPost2.getCategory().getId());
        assertEquals(0, createdPost3.getTags().size());

        this.postId = createdPost1.getId();
    }

    @Test
    public void should_list_all_created_posts() {
        Object response = getListOfPosts("");

        if (response instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<PostDto> posts = (List<PostDto>) response;
            assertNotNull(posts);
            assertEquals(2, posts.size());
            assertEquals("Title 1", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals("Title 2", posts.getLast().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getLast().getStatus());
        } else {
            ApiErrorResponse errorResponse = (ApiErrorResponse) response;
            fail(errorResponse.getStatus() + " ERROR: " + errorResponse.getMessage());
        }
    }

    @Test
    public void should_list_posts_with_specific_category() {
        Object response = getListOfPosts("?categoryId=" + this.category1.getId());

        if (response instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<PostDto> posts = (List<PostDto>) response;
            assertNotNull(posts);
            assertEquals(1, posts.size());
            assertEquals("Title 1", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals(this.category1.getId(), posts.getFirst().getCategory().getId());
        } else {
            ApiErrorResponse errorResponse = (ApiErrorResponse) response;
            fail(errorResponse.getStatus() + " ERROR: " + errorResponse.getMessage());
        }
    }

    @Test
    public void should_list_posts_with_specific_tag() {
        Object response = getListOfPosts("?tagId=" + this.tags.getFirst().getId());

        if (response instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<PostDto> posts = (List<PostDto>) response;
            assertNotNull(posts);
            assertEquals(1, posts.size());
            assertEquals("Title 1", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals(this.tags.getFirst().getId(), posts.getFirst().getTags().stream().toList().getFirst().getId());
        } else {
            ApiErrorResponse errorResponse = (ApiErrorResponse) response;
            fail(errorResponse.getStatus() + " ERROR: " + errorResponse.getMessage());
        }

    }

    @Test
    public void should_list_posts_with_specific_tag_and_category() {
        Object response = getListOfPosts("?tagId=" + this.tags.getLast().getId() + "&categoryId=" + this.category2.getId());

        if (response instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<PostDto> posts = (List<PostDto>) response;
            assertNotNull(posts);
            assertEquals(1, posts.size());
            assertEquals("Title 2", posts.getFirst().getTitle());
            assertEquals(PostStatus.PUBLISHED, posts.getFirst().getStatus());
            assertEquals(this.tags.getLast().getId(), posts.getFirst().getTags().stream().toList().getFirst().getId());
        } else {
            ApiErrorResponse errorResponse = (ApiErrorResponse) response;
            fail(errorResponse.getStatus() + " ERROR: " + errorResponse.getMessage());
        }
    }

    @Test
    public void should_get_existing_post_by_id() {
        PostDto retrievedPost = getPostByItsId();

        assertNotNull(retrievedPost);
        assertEquals(retrievedPost.getId(), this.postId);
    }

    @Test
    public void should_return_error_when_listing_posts_with_nonexistent_category() {
        Object response = getListOfPosts("?categoryId=" + UUID.randomUUID());

        if (response instanceof ApiErrorResponse errorResponse) {
            System.out.println(errorResponse);
            assertNotNull(errorResponse);
            assertEquals(404, errorResponse.getStatus());
            assertEquals("Category not found", errorResponse.getMessage());
            assertNull(errorResponse.getErrors());
        } else {
            fail("Something went wrong");
        }
    }

    private PostDto createPost(CreatePostRequestDto createPostRequestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreatePostRequestDto> requestDtoHttpEntity = new HttpEntity<>(createPostRequestDto, headers);

        try {
            ResponseEntity<PostDto> response = restTemplate.exchange(
                    "/api/v1/posts",
                    HttpMethod.POST,
                    requestDtoHttpEntity,
                    PostDto.class
            );

            System.out.println("Raw JSON response: " + response.getBody());
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());

            if (response.getStatusCode().is2xxSuccessful()) {
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                return response.getBody();
            } else {
                fail("Request failed with status: " + response.getStatusCode() +
                        ", body: " + response.getBody());
                return null;
            }

        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Object getListOfPosts(String requestQueryParams) {
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

            if (response.getStatusCode().is2xxSuccessful()) {
                List<PostDto> posts = objectMapper.readValue(response.getBody(), new TypeReference<>() {
                });
                assertEquals(HttpStatus.OK, response.getStatusCode());
//                assertInstanceOf(List.class, posts);

                return posts;
            } else { // just return ApiErrorResponse object
//                fail("Request failed with status: " + response.getStatusCode() +
//                        ", body: " + response.getBody());
                ApiErrorResponse error = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                return error;
            }
        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public PostDto getPostByItsId() {
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

            if (response.getStatusCode().is2xxSuccessful()) {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                PostDto postDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {
                });
                return postDto;
            } else {
                fail("Request failed with status: " + response.getStatusCode() +
                        ", body: " + response.getBody());
                return null;
            }

        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
