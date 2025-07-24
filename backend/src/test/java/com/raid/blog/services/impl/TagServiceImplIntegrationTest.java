package com.raid.blog.services.impl;

import com.raid.blog.domain.PostStatus;
import com.raid.blog.domain.entities.Category;
import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.domain.entities.User;
import com.raid.blog.repositories.CategoryRepository;
import com.raid.blog.repositories.PostRepository;
import com.raid.blog.repositories.TagRepository;
import com.raid.blog.repositories.UserRepository;
import com.raid.blog.services.TagService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TagServiceImplIntegrationTest {

    private User existingUser;
    private Post existingPost;
    private Tag existingTag;
    private Category existingCategory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void beforeEach() {
        User user = User.builder()
                .name("User")
                .email("user@mail.com")
                .password("password")
                .build();
        this.existingUser = userRepository.save(user);

        Tag tag = Tag.builder()
                .name("Existing Tag")
                .posts(new HashSet<>())
                .build();

        this.existingTag = tagRepository.save(tag);

        Category category = Category.builder()
                .name("Category")
                .build();
        this.existingCategory = categoryRepository.save(category);

        Post post = Post.builder()
                .title("Title")
                .content("This is some content for the post")
                .status(PostStatus.PUBLISHED)
                .author(this.existingUser)
                .tags(Set.of(existingTag))
                .category(existingCategory)
                .readingTime(1) // random just for testing purposes
                .build();

        this.existingPost = postRepository.save(post);
    }

    @Test
    public void should_create_new_tags() {
        // Arrange
        String newTag1 = "New Tag 1";
        String newTag2 = "New Tag 2";
        String existingTag = this.existingTag.getName();
        Set<String> tagNames = Set.of(newTag1, newTag2, existingTag);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Tag> createdTags = tagService.createTags(tagNames);

        // Assert
        assertNotNull(createdTags);
        assertEquals(3, createdTags.size());
        assertEquals(1, tagRepository.countByName("Existing Tag"));
        assertEquals(1, tagRepository.countByName("New Tag 1"));
        assertEquals(1, tagRepository.countByName("New Tag 2"));
        assertEquals(3L, tagRepository.count());

    }

    @Test
    public void should_not_delete_existing_tag_with_posts_associated() {
        // Arrange
        UUID existingTagId = this.existingTag.getId();

        entityManager.flush();
        entityManager.clear();

        // Act
        Exception exception = assertThrows(IllegalStateException.class, () -> tagService.deleteTag(existingTagId));

        // Assert
        assertEquals("Cannot delete tag with posts", exception.getMessage());
        assertEquals(1, tagRepository.countByName("Existing Tag"));
        assertEquals(1L, tagRepository.count());
    }

    @Test
    public void should_delete_existing_tag_with_no_posts_associated() {
        // Arrange
        UUID existingTagId = this.existingTag.getId();
        postRepository.delete(this.existingPost); // existing tag is now not associated to any post

        entityManager.flush();
        entityManager.clear();

        // Act
        tagService.deleteTag(existingTagId);

        // Assert
        assertEquals(0L, tagRepository.count());

    }

    @Test
    public void should_get_existing_tag_by_id() {
        // Arrange
        UUID existingTagId = this.existingTag.getId();

        entityManager.flush();
        entityManager.clear();

        // Act
        Tag foundTag = tagService.getTagById(existingTagId);

        // Assert
        assertNotNull(foundTag);
        assertEquals(foundTag, existingTag);
        assertEquals(1, tagRepository.countByName("Existing Tag"));
    }

    @Test
    public void should_throw_exception_when_getting_nonexistent_tag_id() {
        // Arrange
        UUID existingTagId = UUID.randomUUID();

        entityManager.flush();
        entityManager.clear();

        // Act
        Exception exception = assertThrows(EntityNotFoundException.class, () -> tagService.getTagById(existingTagId));

        // Assert
        assertEquals("No tag was found", exception.getMessage());
    }

    @Test
    public void should_get_existing_tags_by_ids() {
        // Arrange
        Tag newTag1 = Tag.builder().name("New Tag 1").build();
        Tag newTag2 = Tag.builder().name("New Tag 2").build();
        List<Tag> savedNewTags = tagRepository.saveAll(List.of(newTag1, newTag2));
        Set<UUID> tagIds = Set.of(this.existingTag.getId(), savedNewTags.getFirst().getId(), savedNewTags.getLast().getId());

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Tag> foundTags = tagService.getTagsByIds(tagIds);

        // Assert
        assertNotNull(foundTags);
        assertEquals(3, foundTags.size());
        assertTrue(foundTags.contains(this.existingTag));
        assertTrue(foundTags.contains(savedNewTags.getFirst()));
        assertTrue(foundTags.contains(savedNewTags.getLast()));
    }
}
