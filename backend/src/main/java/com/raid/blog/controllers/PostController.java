package com.raid.blog.controllers;

import com.raid.blog.domain.CreatePostRequest;
import com.raid.blog.domain.UpdatePostRequest;
import com.raid.blog.domain.dtos.CreatePostRequestDto;
import com.raid.blog.domain.dtos.PostDto;
import com.raid.blog.domain.dtos.UpdatePostRequestDto;
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

    @GetMapping("{id}")
    public ResponseEntity<PostDto> getPost(
            @PathVariable UUID id
    ) {
        Post post = postService.getPost(id);
        PostDto postDto = postMapper.toDto(post);

        return ResponseEntity.ok(postDto);
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

    @PutMapping("{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePostRequestDto request
    ) {
        UpdatePostRequest updatePostRequest = postMapper.toUpdatePostRequest(request);
        Post updatedPost = postService.updatePost(id, updatePostRequest);
        PostDto updatedPostDto = postMapper.toDto(updatedPost);

        return new ResponseEntity<>(updatedPostDto, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID id
    ) {
        postService.deletePost(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
