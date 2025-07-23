package com.raid.blog.services.impl;

import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.repositories.TagRepository;
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
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    public void should_get_all_tags_with_posts_associated() {
        // Arrange
        Tag tag = Tag.builder().id(UUID.randomUUID()).name("Tag").build();

        Post post1 = Post.builder().id(UUID.randomUUID()).build();
        Post post2 = Post.builder().id(UUID.randomUUID()).build();

        post1.setTags(Set.of(tag));
        post2.setTags(Set.of(tag));

        tag.setPosts(Set.of(post1, post2));

        // What to return
        when(tagRepository.findAllWithPostCount()).thenReturn(List.of(tag));

        // Act
        List<Tag> tags = tagService.getTags();

        // Assert
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals(2, tags.getFirst().getPosts().size());

        Set<UUID> expectedPostIds = Set.of(post1.getId(), post2.getId());
        Set<UUID> actualPostIds = tags.getFirst().getPosts().stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        assertEquals(expectedPostIds, actualPostIds);

        verify(tagRepository, times(1)).findAllWithPostCount();
    }

    @Test
    public void should_only_create_new_tags() {
        // Arrange
        Set<String> tagNames = Set.of("New Tag 1", "New Tag 2", "Existing Tag");
        List<Tag> existingTags = List.of(Tag.builder().id(UUID.randomUUID()).name("Existing Tag").build());
        List<Tag> newTags = new ArrayList<>();
        newTags.add(Tag.builder().id(UUID.randomUUID()).name("New Tag 1").build());
        newTags.add(Tag.builder().id(UUID.randomUUID()).name("New Tag 2").build());

        List<Tag> allSavedTags = new ArrayList<>();
        allSavedTags.add(newTags.getFirst());
        allSavedTags.add(newTags.get(1));

        // What to return
        when(tagRepository.findByNameIn(tagNames)).thenReturn(existingTags);
        when(tagRepository.saveAll(any(List.class))).thenReturn(allSavedTags);

        // Act
        List<Tag> savedTags = tagService.createTags(tagNames);

        // Assert
        assertNotNull(savedTags);
        assertNotEquals(savedTags, newTags);
        assertEquals(3, savedTags.size());

        verify(tagRepository, times(1)).findByNameIn(tagNames);
        verify(tagRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    public void should_delete_tags_with_no_posts_associated() {
        // Arrange
        UUID tagId = UUID.randomUUID();
        Tag existingTag = Tag.builder().id(tagId).posts(Set.of()).build();

        // What to return
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));

        // Act
        tagService.deleteTag(tagId);

        // Assert
        assertNotNull(existingTag);

        verify(tagRepository, times(1)).findById(tagId);
        verify(tagRepository, times(1)).deleteById(tagId);
    }

    @Test
    public void should_throw_exception_when_deleting_tag_posts_associated() {
        // Arrange
        UUID tagId = UUID.randomUUID();
        Tag existingTag = Tag.builder().build();
        existingTag.setPosts(Set.of(
                Post.builder().id(UUID.randomUUID()).tags(Set.of(existingTag)).build()
        ));
        // What to return
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));

        // Act
        Exception exception = assertThrows(IllegalStateException.class, () -> tagService.deleteTag(tagId));

        // Assert
        assertEquals("Cannot delete tag with posts", exception.getMessage());
    }

    @Test
    public void should_get_existing_tag_by_id() {
        // Arrange
        UUID tagId = UUID.randomUUID();
        Tag existingTag = Tag.builder().id(tagId).build();

        // What to return
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));

        // Act
        Tag tag = tagService.getTagById(tagId);

        // Assert
        assertNotNull(tag);
        assertEquals(tag, existingTag);

        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    public void should_throw_exception_when_getting_nonexisting_tag() {
        // Arrange
        UUID tagId = UUID.randomUUID();
        // What to return
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        // Act
        Exception exception = assertThrows(EntityNotFoundException.class, () -> tagService.getTagById(tagId));

        // Assert
        assertEquals("No tag was found", exception.getMessage());
    }

}