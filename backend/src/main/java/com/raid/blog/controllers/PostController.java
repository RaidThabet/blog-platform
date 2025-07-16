package com.raid.blog.controllers;

import com.raid.blog.domain.dtos.PostDto;
import com.raid.blog.mappers.PostMapper;
import com.raid.blog.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts(
            @RequestParam(required = false)UUID categoryId,
            @RequestParam(required = false)UUID tagId
    ) {
        var posts = postService.getAllPosts(categoryId, tagId).stream()
                .map(postMapper::toDto)
                .toList();

        return ResponseEntity.ok(posts);
    }
}
