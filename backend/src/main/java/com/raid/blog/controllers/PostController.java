package com.raid.blog.controllers;

import com.raid.blog.domain.CreatePostRequest;
import com.raid.blog.domain.dtos.CreatePostRequestDto;
import com.raid.blog.domain.dtos.PostDto;
import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.User;
import com.raid.blog.mappers.PostMapper;
import com.raid.blog.services.PostService;
import com.raid.blog.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final UserService userService;
    private final PostService postService;
    private final PostMapper postMapper;

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId
    ) {
        var posts = postService.getAllPosts(categoryId, tagId).stream()
                .map(postMapper::toDto)
                .toList();

        return ResponseEntity.ok(posts);
    }

    @GetMapping("drafts")
    public ResponseEntity<List<PostDto>> getDrafts(
            @RequestAttribute UUID userId
    ) {
        User loggedInUser = userService.getUserById(userId);
        List<Post> draftPosts = postService.getDraftPosts(loggedInUser);
        var postDTOs = draftPosts.stream().map(postMapper::toDto).toList();
        return ResponseEntity.ok(postDTOs);
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestBody @Valid CreatePostRequestDto request,
            @RequestAttribute UUID userId
    ) {
        User loggedInUser = userService.getUserById(userId);
        CreatePostRequest createPostRequest = postMapper.toCreatePostRequest(request);
        Post createdPOst = postService.createPost(loggedInUser, createPostRequest);
        PostDto createdPostDto = postMapper.toDto(createdPOst);

        return new ResponseEntity<>(createdPostDto, HttpStatus.CREATED);
    }
}
