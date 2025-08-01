package com.raid.blog.mappers;

import com.raid.blog.domain.CreatePostRequest;
import com.raid.blog.domain.UpdatePostRequest;
import com.raid.blog.domain.dtos.CreatePostRequestDto;
import com.raid.blog.domain.dtos.PostDto;
import com.raid.blog.domain.dtos.UpdatePostRequestDto;
import com.raid.blog.domain.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    PostDto toDto(Post post);

    @Mapping(target = "status", source = "status")
    CreatePostRequest toCreatePostRequest(CreatePostRequestDto createPostRequestDto);

    @Mapping(target = "status", source = "status")
    UpdatePostRequest toUpdatePostRequest(UpdatePostRequestDto updatePostRequestDto);
}
