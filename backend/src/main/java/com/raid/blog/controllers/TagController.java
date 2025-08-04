package com.raid.blog.controllers;

import com.raid.blog.domain.dtos.CreateTagsRequest;
import com.raid.blog.domain.dtos.TagDto;
import com.raid.blog.mappers.TagMapper;
import com.raid.blog.services.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getTags().stream()
                .map(tagMapper::toTagResponse)
                .toList();

        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<List<TagDto>> createTags(
            @RequestBody @Valid CreateTagsRequest request
    ) {
        var savedTags = tagService.createTags(request.getNames());
        var createdTagResponse = savedTags.stream().map(tagMapper::toTagResponse).toList();
        return new ResponseEntity<>(
                createdTagResponse,
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable UUID id
    ) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

}
