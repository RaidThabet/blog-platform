package com.raid.blog.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raid.blog.config.TestSecurityConfig;
import com.raid.blog.domain.CreatePostRequest;
import com.raid.blog.domain.PostStatus;
import com.raid.blog.domain.dtos.*;
import com.raid.blog.domain.entities.Category;
import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.domain.entities.User;
import com.raid.blog.mappers.PostMapper;
import com.raid.blog.services.AuthenticationService;
import com.raid.blog.services.PostService;
import com.raid.blog.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(TestSecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    public void should_list_all_posts_with_category_and_tag_filters() throws Exception {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Post post1 = Post.builder()
                .title("Title 1")
                .category(Category.builder().id(categoryId).build())
                .tags(Set.of(Tag.builder().id(tagId).build()))
                .build();
        Post post2 = Post.builder()
                .title("Title 2")
                .category(Category.builder().id(categoryId).build())
                .tags(Set.of(Tag.builder().id(tagId).build()))
                .build();

        PostDto post1Dto = PostDto
                .builder()
                .title("Title 1")
                .category(CategoryDto.builder().id(categoryId).build())
                .tags(Set.of(TagDto.builder().id(tagId).build()))
                .build();

        PostDto post2Dto = PostDto
                .builder()
                .title("Title 1")
                .category(CategoryDto.builder().id(categoryId).build())
                .tags(Set.of(TagDto.builder().id(tagId).build()))
                .build();

        List<Post> expectedPosts = List.of(post1, post2);

        // What to return
        given(postService.getAllPosts(categoryId, tagId)).willReturn(expectedPosts);
        given(postMapper.toDto(post1)).willReturn(post1Dto);
        given(postMapper.toDto(post2)).willReturn(post2Dto);

        // Act and expect
        var mockMvcResponse = mockMvc
                .perform(
                        get("/api/v1/posts")
                                .param("categoryId", String.valueOf(categoryId))
                                .param("tagId", String.valueOf(tagId))
                )
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = mockMvcResponse.getResponse().getContentAsString();
        List<PostDto> returnedPosts = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(2, returnedPosts.size());
        assertEquals(post1Dto, returnedPosts.getFirst());
        assertEquals(post2Dto, returnedPosts.getLast());
    }

    @WithMockUser
    @Test
    public void should_get_existing_post_by_id() throws Exception {
        // Arrange
        UUID postId = UUID.randomUUID();
        Post post = Post.builder().id(postId).build();
        PostDto postDto = PostDto.builder().id(postId).build();

        // What to return
        given(postService.getPost(postId)).willReturn(post);
        given(postMapper.toDto(post)).willReturn(postDto);

        // Act and expect
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/api/v1/posts/" + postId)
                )
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        PostDto returnedPostDto = objectMapper.readValue(responseJson, PostDto.class);
        assertNotNull(returnedPostDto);
        assertEquals(postDto, returnedPostDto);
    }

    @WithMockUser
    @Test
    public void should_fail_when_getting_nonexistent_post() throws Exception {
        // Arrange
        UUID postId = UUID.randomUUID();

        // What to return
        given(postService.getPost(postId)).willThrow(EntityNotFoundException.class);

        // Act and expect
        mockMvc
                .perform(
                        get("/api/v1/posts/" + postId)
                )
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    public void should_get_draft_posts() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Post post1 = Post.builder()
                .title("Title 1")
                .status(PostStatus.DRAFT)
                .build();
        Post post2 = Post.builder()
                .title("Title 2")
                .status(PostStatus.DRAFT)
                .build();

        PostDto post1Dto = PostDto
                .builder()
                .title("Title 1")
                .status(PostStatus.DRAFT)
                .build();

        PostDto post2Dto = PostDto
                .builder()
                .title("Title 1")
                .status(PostStatus.DRAFT)
                .build();

        List<Post> expectedPosts = List.of(post1, post2);
        List<PostDto> expectedPostsDtos = List.of(post1Dto, post2Dto);

        // What to return
        given(userService.getUserById(userId)).willReturn(user);
        given(postService.getDraftPosts(user)).willReturn(expectedPosts);
        given(postMapper.toDto(post1)).willReturn(post1Dto);
        given(postMapper.toDto(post2)).willReturn(post2Dto);

        // Act and expect
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/api/v1/posts/drafts")
                                .with(request -> {
                                    request.setAttribute("userId", userId);
                                    return request;
                                })
                )
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        List<PostDto> postDtos = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertNotNull(postDtos);
        assertEquals(2, postDtos.size());
        assertEquals(expectedPostsDtos, postDtos);
    }

    @WithMockUser
    @Test
    public void should_create_new_post_with_valid_request() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        CreatePostRequestDto createPostRequestDto = CreatePostRequestDto.builder()
                .title("New Title")
                .content("New Content")
                .status(PostStatus.PUBLISHED)
                .categoryId(UUID.randomUUID())
                .tagIds(Set.of())
                .build();
        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("New Title")
                .content("New Content")
                .status(PostStatus.PUBLISHED)
                .categoryId(createPostRequestDto.getCategoryId())
                .tagIds(Set.of())
                .build();
        String requestJson = objectMapper.writeValueAsString(createPostRequest);
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .title("New Title")
                .content("New Content")
                .status(PostStatus.PUBLISHED)
                .category(Category.builder().id(createPostRequestDto.getCategoryId()).build())
                .tags(Set.of())
                .author(user)
                .readingTime(1)
                .build();
        PostDto postDto = PostDto.builder()
                .id(post.getId())
                .title("New Title")
                .content("New Content")
                .category(CategoryDto.builder().id(post.getCategory().getId()).build())
                .author(AuthorDto.builder().id(user.getId()).name(user.getName()).build())
                .tags(Set.of())
                .build();

        // What to return
        given(userService.getUserById(userId)).willReturn(user);
        given(postMapper.toCreatePostRequest(createPostRequestDto)).willReturn(createPostRequest);
        given(postService.createPost(user, createPostRequest)).willReturn(post);
        given(postMapper.toDto(post)).willReturn(postDto);

        // Act and expect
        MvcResult mvcResult = mockMvc
                .perform(
                        post("/api/v1/posts")
                                .contentType("application/json")
                                .content(requestJson)
                                .with(request -> {
                                    request.setAttribute("userId", userId);
                                    return request;
                                })
                )
                .andExpect(status().isCreated())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        PostDto createdPostDto = objectMapper.readValue(responseJson, PostDto.class);
        assertNotNull(createdPostDto);
        assertEquals(postDto, createdPostDto);
    }
}