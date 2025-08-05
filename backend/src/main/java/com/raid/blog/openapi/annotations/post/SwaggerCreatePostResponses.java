package com.raid.blog.openapi.annotations.post;

import com.raid.blog.domain.dtos.ApiErrorResponse;
import com.raid.blog.domain.dtos.PostDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created new post for authenticated user",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = PostDto.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid creation request",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"status\":400,\"message\":\"Validation failed\",\"errors\":[{\"field\":\"title\",\"message\":\"Title is required\"},{\"field\":\"content\",\"message\":\"Content must be between 10 and 50000 characters\"},{\"field\":\"title\",\"message\":\"Title must be between 3 and 200 characters\"},{\"field\":\"content\",\"message\":\"Content is required\"}]}\n"
                        )
                )),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"status\":404,\"message\":\"User not found\",\"errors\":null}"
                        )
                ))})
public @interface SwaggerCreatePostResponses {
}
