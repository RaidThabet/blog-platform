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
import com.raid.blog.services.PostService;
import com.raid.blog.services.TagService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final CategoryService categoryService;
    private final TagService tagService;

    private static final int WORDS_PER_MINUTE = 200;

    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPosts(UUID categoryId, UUID tagId) {
        if (categoryId != null && tagId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            Tag tag = tagService.getTagById(tagId);
            return postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatus.PUBLISHED,
                    category,
                    tag
            );
        }
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            return postRepository.findAllByStatusAndCategory(
                    PostStatus.PUBLISHED,
                    category
            );
        }
        if (tagId != null) {
            Tag tag = tagService.getTagById(tagId);
            return postRepository.findAllByStatusAndTagsContaining(
                    PostStatus.PUBLISHED,
                    tag
            );
        }
        return postRepository.findAllByStatus(PostStatus.PUBLISHED);
    }

    @Override
    public List<Post> getDraftPosts(User user) {
        return postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT);
    }

    @Override
    @Transactional
    public Post createPost(User user, CreatePostRequest createPostRequest) {
        var readingTime = calculateReadingTime(createPostRequest.getContent());
        Category category = categoryService.getCategoryById(createPostRequest.getCategoryId());
        List<Tag> tags = tagService.getTagsByIds(createPostRequest.getTagIds());
        Post newPost = Post.builder()
                .title(createPostRequest.getTitle())
                .content(createPostRequest.getContent())
                .status(createPostRequest.getStatus())
                .readingTime(readingTime)
                .author(user)
                .category(category)
                .tags(new HashSet<>(tags))
                .build();

        Post savedPost = postRepository.save(newPost);

        return savedPost;
    }

    private Integer calculateReadingTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int wordCount = content.trim().split("\\s+").length;
        return (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);
    }

    @Override
    @Transactional
    public Post updatePost(UUID id, UpdatePostRequest updatePostRequest) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist"));

        existingPost.setTitle(updatePostRequest.getTitle());
        existingPost.setContent(updatePostRequest.getContent());
        existingPost.setStatus(updatePostRequest.getStatus());

        UUID updatePostRequestCategoryId = updatePostRequest.getCategoryId();
        if (!existingPost.getCategory().getId().equals(updatePostRequestCategoryId)) {
            Category category = categoryService.getCategoryById(updatePostRequest.getCategoryId());
            existingPost.setCategory(category);
        }

        Set<UUID> existingTagIds = existingPost.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
        Set<UUID> updatePostRequestTagIds = updatePostRequest.getTagIds();
        if (!existingTagIds.equals(updatePostRequestTagIds)) {
            List<Tag> tags = tagService.getTagsByIds(updatePostRequest.getTagIds());
            existingPost.setTags(new HashSet<>(tags));
        }

        return postRepository.save(existingPost);
    }
}
