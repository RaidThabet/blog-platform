package com.raid.blog.controllers;

import com.raid.blog.domain.dtos.CreateTagsRequest;
import com.raid.blog.domain.dtos.TagDto;
import com.raid.blog.mappers.TagMapper;
import com.raid.blog.openapi.annotations.tag.SwaggerCreateTagsResponses;
import com.raid.blog.openapi.annotations.tag.SwaggerDeleteTagResponses;
import com.raid.blog.openapi.annotations.tag.SwaggerGetAllTagsResponses;
import com.raid.blog.services.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tag", description = "Describes the different endpoints related to Tag")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @Operation(summary = "Get list of all tags")
    @SwaggerGetAllTagsResponses
    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getTags().stream()
                .map(tagMapper::toTagResponse)
                .toList();

        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Create a new tag")
    @SwaggerCreateTagsResponses
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

    @Operation(summary = "Delete an existing tag")
    @SwaggerDeleteTagResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable UUID id
    ) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

}
