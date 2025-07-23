package com.raid.blog.services.impl;

import com.raid.blog.domain.CreatePostRequest;
import com.raid.blog.domain.PostStatus;
import com.raid.blog.domain.UpdatePostRequest;
import com.raid.blog.domain.entities.Category;
import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.domain.entities.User;
import com.raid.blog.repositories.PostRepository;
import com.raid.blog.services.CategoryService;
import com.raid.blog.services.TagService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    public void should_get_all_published_posts_if_no_filters() {
        // Arrange
        Category category1 = new Category();
        Category category2 = new Category();

        category1.setId(UUID.randomUUID());
        category1.setName("Category 1");

        category2.setId(UUID.randomUUID());
        category2.setName("Category 2");

        Tag tag1 = new Tag();
        Tag tag2 = new Tag();

        tag1.setId(UUID.randomUUID());
        tag1.setName("Tag 1");

        tag2.setId(UUID.randomUUID());
        tag2.setName("Tag 2");

        UUID categoryId = null;
        UUID tagId = null;

        List<Post> allPosts = Arrays.asList(
                Post.builder()
                        .id(UUID.randomUUID())
                        .title("Test Post 1")
                        .content("Test content 1")
                        .status(PostStatus.DRAFT)
                        .category(category1)
                        .tags(Set.of(tag1))
                        .build(),
                Post.builder()
                        .id(UUID.randomUUID())
                        .title("Test Post 2")
                        .content("Test content 2")
                        .status(PostStatus.PUBLISHED)
                        .category(category2)
                        .tags(Set.of(tag2))
                        .build()
        );

        List<Post> expectedPosts = allPosts.subList(1, 2);

        // What to return
        when(postRepository.findAllByStatus(PostStatus.PUBLISHED)).thenReturn(expectedPosts);

        // Act
        List<Post> result = postService.getAllPosts(categoryId, tagId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPosts, result);
        assertEquals(1, result.size());

        verify(postRepository, times(1)).findAllByStatus(PostStatus.PUBLISHED);
    }

    @Test
    public void should_return_published_posts_with_specified_category() {
        // Arrangevs
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);

        List<Post> expectedPosts = Collections.singletonList(
                Post.builder()
                        .id(UUID.randomUUID())
                        .category(category)
                        .status(PostStatus.PUBLISHED)
                        .build()
        );

        // What to return
        when(categoryService.getCategoryById(categoryId)).thenReturn(category);
        when(postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, category)).thenReturn(expectedPosts);

        // Act
        List<Post> result = postService.getAllPosts(categoryId, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedPosts, result);

        verify(postRepository, times(1)).findAllByStatusAndCategory(PostStatus.PUBLISHED, category);
    }

    @Test
    public void should_return_published_posts_with_specified_tag() {
        // Arrange
        UUID tagId = UUID.randomUUID();
        Tag tag = new Tag();
        tag.setId(tagId);

        List<Post> expectedPosts = Collections.singletonList(
                Post.builder()
                        .id(UUID.randomUUID())
                        .tags(Set.of(tag))
                        .status(PostStatus.PUBLISHED)
                        .build()
        );

        // What to return
        when(tagService.getTagById(tagId)).thenReturn(tag);
        when(postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag)).thenReturn(expectedPosts);

        // Act
        List<Post> result = postService.getAllPosts(null, tagId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedPosts, result);

        verify(postRepository, times(1)).findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag);
    }

    @Test
    public void should_get_post_by_id() {
        // Arrange
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        post.setId(postId);

        // What to return
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // Act
        Post retrievedPost = postService.getPost(postId);

        // Assert
        assertNotNull(retrievedPost);
        assertEquals(retrievedPost.getId(), postId);

        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    public void should_throw_exception_when_post_does_not_exist() {
        // Arrange
        UUID nonExistentPostId = UUID.randomUUID();

        // What to return
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // Act
        Exception exp = assertThrows(EntityNotFoundException.class, () -> postService.getPost(nonExistentPostId));

        // Assert
        assertEquals("Post does not exist", exp.getMessage());
        verify(postRepository, times(1)).findById(nonExistentPostId);
    }

    @Test
    public void should_retrieve_draft_posts() {
        // Arrange
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .author(user)
                .title("Title 1")
                .content("Content 1")
                .status(PostStatus.DRAFT)
                .build();

        List<Post> draftedPosts = Collections.singletonList(post);

        // What to return
        when(postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT)).thenReturn(draftedPosts);

        // Act
        List<Post> retrievedPosts = postService.getDraftPosts(user);

        // Assert
        assertNotNull(retrievedPosts);
        assertEquals(draftedPosts, retrievedPosts);

        verify(postRepository, times(1)).findAllByAuthorAndStatus(user, PostStatus.DRAFT);
    }

    @Test
    public void should_create_post() {
        // Arrange
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("User 1")
                .email("smth@mail.com")
                .build();

        UUID categoryId = UUID.randomUUID();
        Category category = Category
                .builder()
                .id(categoryId)
                .name("Category")
                .build();

        UUID tagId1 = UUID.randomUUID();
        UUID tagId2 = UUID.randomUUID();

        List<Tag> tags = List.of(
                Tag
                        .builder()
                        .id(tagId1)
                        .name("Tag 1")
                        .build(),
                Tag
                        .builder()
                        .id(tagId2)
                        .name("Tag 2")
                        .build()
        );

        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("Test Post Title")
                .content("This is a test post content with multiple words to test reading time calculation.")
                .status(PostStatus.PUBLISHED)
                .categoryId(categoryId)
                .tagIds(Set.of(tagId1, tagId2))
                .build();

        // What to return
        when(categoryService.getCategoryById(createPostRequest.getCategoryId())).thenReturn(category);
        when(tagService.getTagsByIds(createPostRequest.getTagIds())).thenReturn(tags);
        when(postRepository.save(any(Post.class))).thenReturn(
                Post
                        .builder()
                        .id(UUID.randomUUID())
                        .title(createPostRequest.getTitle())
                        .content(createPostRequest.getContent())
                        .category(category)
                        .tags(new HashSet<>(tags))
                        .author(user)
                        .readingTime(2)
                        .status(createPostRequest.getStatus())
                        .build()
        );

        // Act
        Post savedPost = postService.createPost(user, createPostRequest);

        // Assert
        assertNotNull(savedPost);
        assertEquals(savedPost.getTitle(), createPostRequest.getTitle());
        assertEquals(savedPost.getContent(), createPostRequest.getContent());
        assertEquals(savedPost.getTags(), new HashSet<>(tags));
        assertEquals(savedPost.getAuthor(), user);
        assertEquals(savedPost.getCategory(), category);
        assertEquals(2, savedPost.getReadingTime());
        assertEquals(savedPost.getStatus(), createPostRequest.getStatus());

        verify(categoryService, times(1)).getCategoryById(categoryId);
        verify(tagService, times(1)).getTagsByIds(createPostRequest.getTagIds());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    public void should_update_existing_post() {
        // Arrange
        UUID postId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID tag1Id = UUID.randomUUID();
        UUID tag2Id = UUID.randomUUID();
        UpdatePostRequest updatePostRequest = UpdatePostRequest
                .builder()
                .id(postId)
                .categoryId(categoryId)
                .tagIds(Set.of(tag1Id, tag2Id))
                .title("New title 1")
                .content("New content 2")
                .status(PostStatus.PUBLISHED)
                .build();

        Category category = Category
                .builder()
                .id(categoryId)
                .name("Category 1")
                .build();

        Tag tag1 = Tag
                .builder()
                .id(tag1Id)
                .name("Tag 1")
                .build();

        Tag tag2 = Tag
                .builder()
                .id(tag2Id)
                .name("Tag 2")
                .build();

        Post existingPost = Post
                .builder()
                .id(postId)
                .title("Title 1")
                .content("Content 1")
                .category(Category.builder().id(UUID.randomUUID()).build())
                .status(PostStatus.DRAFT)
                .tags(Set.of(tag1))
                .build();

        // What to return
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(categoryService.getCategoryById(categoryId)).thenReturn(category);
        when(tagService.getTagsByIds(updatePostRequest.getTagIds())).thenReturn(List.of(tag1, tag2));
        when(postRepository.save(any(Post.class))).thenReturn(existingPost);

        // Act
        Post savedPost = postService.updatePost(postId, updatePostRequest);

        // Assert
        assertNotNull(savedPost);
        assertEquals(savedPost.getTitle(), updatePostRequest.getTitle());
        assertEquals(savedPost.getContent(), updatePostRequest.getContent());
        assertEquals(savedPost.getStatus(), updatePostRequest.getStatus());
        assertEquals(savedPost.getCategory().getId(), updatePostRequest.getCategoryId());
        assertEquals(savedPost.getTags().stream().map(Tag::getId).collect(Collectors.toSet()), updatePostRequest.getTagIds());

        verify(postRepository, times(1)).findById(postId);
        verify(categoryService, times(1)).getCategoryById(categoryId);
        verify(tagService, times(1)).getTagsByIds(updatePostRequest.getTagIds());
    }

    @Test
    public void should_throw_exception_when_updating_non_existent_post() {
        // Arrange
        UUID postId = UUID.randomUUID();
        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder().build();

        // What to return
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act
        Exception exception = assertThrows(EntityNotFoundException.class, () -> postService.updatePost(postId, updatePostRequest));

        // Assert
        assertEquals("Post does not exist", exception.getMessage());
    }

    @Test
    public void should_delete_post() {
        // Arrange
        UUID postId = UUID.randomUUID();
        Post existingPost = Post
                .builder()
                .id(postId)
                .build();

        // What to return
        when(postRepository.findById(postId)).thenReturn(Optional.ofNullable(existingPost));

        // Act
        postService.deletePost(postId);

        // Assert
        assertNotNull(existingPost);
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).delete(existingPost);
    }

    @Test
    public void should_throw_exception_when_deleting_nonexisting_post() {
        // Arrange
        UUID postId = UUID.randomUUID();

        // What to return
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act
        Exception exception = assertThrows(EntityNotFoundException.class, () -> postService.deletePost(postId));

        // Assert
        assertEquals("Post does not exist", exception.getMessage());
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(0)).delete(any(Post.class));
    }
}