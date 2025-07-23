package com.raid.blog.services.impl;

import com.raid.blog.domain.CreatePostRequest;
import com.raid.blog.domain.PostStatus;
import com.raid.blog.domain.UpdatePostRequest;
import com.raid.blog.domain.entities.Category;
import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.domain.entities.User;
import com.raid.blog.repositories.CategoryRepository;
import com.raid.blog.repositories.PostRepository;
import com.raid.blog.repositories.TagRepository;
import com.raid.blog.repositories.UserRepository;
import com.raid.blog.services.PostService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostServiceImplIntegrationTest {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    public void should_create_post_with_real_database() {
        // Arrange
        User user = User.builder()
                .name("Name")
                .email("name@example.com")
                .password("some password")
                .build();
        user = userRepository.save(user);

        Category category = Category.builder()
                .name("Technology")
                .build();
        category = categoryRepository.save(category); // persist to generate an ID

        Tag tag1 = Tag.builder().name("Java").build();
        Tag tag2 = Tag.builder().name("Spring").build();
        tag1 = tagRepository.save(tag1);
        tag2 = tagRepository.save(tag2);

        CreatePostRequest request = CreatePostRequest.builder()
                .title("Title")
                .content("This is a test post with sufficient content to calculate reading time")
                .status(PostStatus.PUBLISHED)
                .categoryId(category.getId())
                .tagIds(Set.of(tag1.getId(), tag2.getId()))
                .build();

        // Act
        Post createdPost = postService.createPost(user, request);

        // Assert
        assertNotNull(createdPost.getId());
        assertEquals(request.getTitle(), createdPost.getTitle());
        assertEquals(request.getContent(), createdPost.getContent());
        assertEquals(user.getId(), createdPost.getAuthor().getId());
        assertEquals(category.getId(), createdPost.getCategory().getId());
        assertEquals(2, createdPost.getTags().size());
        assertTrue(createdPost.getReadingTime() > 0);

        // Verify persistence
        Post savedPost = postRepository.findById(createdPost.getId()).orElse(null);
        assertNotNull(savedPost);
        assertEquals(createdPost.getTitle(), savedPost.getTitle());
    }

    @Test
    public void should_update_post_and_persist_changes() {
        // Arrange
        User user = userRepository.save(User.builder().name("Name").email("name@mail.com").password("some pass").build());
        Category oldCategory = categoryRepository.save(Category.builder().name("Category").build());
        Category newCategory = categoryRepository.save(Category.builder().name("New category").build());

        Post existingPOst = Post.builder()
                .title("Title")
                .content("Content")
                .status(PostStatus.DRAFT)
                .author(user)
                .category(oldCategory)
                .tags(Set.of())
                .readingTime(1)
                .build();
        existingPOst = postRepository.save(existingPOst);

        UpdatePostRequest updateRequest = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated content")
                .status(PostStatus.PUBLISHED)
                .categoryId(newCategory.getId())
                .tagIds(Set.of())
                .build();

        // Act
        Post updatedPost = postService.updatePost(existingPOst.getId(), updateRequest);

        // Assert
        assertEquals(updateRequest.getTitle(), updatedPost.getTitle());
        assertEquals(updateRequest.getContent(), updatedPost.getContent());
        assertEquals(updateRequest.getStatus(), updatedPost.getStatus());
        assertEquals(updateRequest.getCategoryId(), updatedPost.getCategory().getId());
        assertEquals(0, updatedPost.getTags().size());
        assertEquals(newCategory.getId(), updatedPost.getCategory().getId());

        // Verify persistence
        Post persistedPost = postRepository.findById(existingPOst.getId()).orElse(null);
        assertNotNull(persistedPost);
        assertEquals(updatedPost.getTitle(), persistedPost.getTitle());
        assertEquals(updatedPost.getContent(), persistedPost.getContent());
        assertEquals(updatedPost.getCategory(), persistedPost.getCategory());
        assertEquals(updatedPost.getTags().size(), persistedPost.getTags().size());
        assertEquals(updatedPost.getReadingTime(), persistedPost.getReadingTime());
    }

    @Test
    public void should_delete_post_from_database() {
        // Arrange
        User user = userRepository.save(User.builder().name("User name").email("test@example.com").password("some pass").build());
        Category category = categoryRepository.save(Category.builder().name("Category").build());
        Tag tag = tagRepository.save(Tag.builder().name("TAg").build());

        Post post = Post.builder()
                .title("Title")
                .content("Content")
                .status(PostStatus.DRAFT)
                .author(user)
                .category(category)
                .tags(Set.of(tag))
                .readingTime(1)
                .build();
        post = postRepository.save(post);
        UUID postId = post.getId();

        // Verify post exists
        assertTrue(postRepository.findById(postId).isPresent());

        // Act
        postService.deletePost(postId);

        // Assert
        assertFalse(postRepository.findById(postId).isPresent());
        assertTrue(categoryRepository.findById(category.getId()).isPresent());
        assertTrue(tagRepository.findById(tag.getId()).isPresent());
    }

    @Test
    public void should_handle_tag_relationships_correctly() {
        // Arrange
        User user = userRepository.save(User.builder().name("User").email("test@example.com").password("some pass").build());
        Category category = categoryRepository.save(Category.builder().name("Test Category").build());

        Tag tag1 = tagRepository.save(Tag.builder().name("Java").build());
        Tag tag2 = tagRepository.save(Tag.builder().name("Spring Boot").build());
        Tag tag3 = tagRepository.save(Tag.builder().name("Testing").build());

        CreatePostRequest request = CreatePostRequest.builder()
                .title("Title")
                .content("This post has multiple tags")
                .status(PostStatus.PUBLISHED)
                .categoryId(category.getId())
                .tagIds(Set.of(tag1.getId(), tag2.getId()))
                .build();

        // Act - Create post with tags
        Post createdPost = postService.createPost(user, request);

        // Assert - Verify tags are properly associated
        assertEquals(2, createdPost.getTags().size());

        // Act - Update to different tags
        UpdatePostRequest updateRequest = UpdatePostRequest.builder()
                .title("Tagged Post")
                .content("This post has updated tags")
                .status(PostStatus.PUBLISHED)
                .categoryId(category.getId())
                .tagIds(Set.of(tag2.getId(), tag3.getId()))
                .build();

        Post updatedPost = postService.updatePost(createdPost.getId(), updateRequest);

        // Assert - Verify tags are updated
        assertEquals(2, updatedPost.getTags().size());
        assertTrue(updatedPost.getTags().stream()
                .anyMatch(tag -> tag.getId().equals(tag2.getId())));
        assertTrue(updatedPost.getTags().stream()
                .anyMatch(tag -> tag.getId().equals(tag3.getId())));
        assertFalse(updatedPost.getTags().stream()
                .anyMatch(tag -> tag.getId().equals(tag1.getId())));
    }

    @Test
    public void should_calculate_reading_time_accurately_with_real_content() {
        // Arrange
        User user = userRepository.save(User.builder().name("User").email("test@example.com").password("some pass").build());
        Category category = categoryRepository.save(Category.builder().name("Category").build());

        // Create content with exactly 600 words (should be 3 minutes at 200 words/minute)
        String content = "word ".repeat(600).trim();

        CreatePostRequest request = CreatePostRequest.builder()
                .title("Title")
                .content(content)
                .status(PostStatus.PUBLISHED)
                .categoryId(category.getId())
                .tagIds(Set.of())
                .build();

        // Act
        Post createdPost = postService.createPost(user, request);

        // Assert
        assertEquals(3, createdPost.getReadingTime());

        // Verify persistence
        Post persistedPost = postRepository.findById(createdPost.getId()).orElse(null);
        assertNotNull(persistedPost);
        assertEquals(3, persistedPost.getReadingTime());
    }


}
