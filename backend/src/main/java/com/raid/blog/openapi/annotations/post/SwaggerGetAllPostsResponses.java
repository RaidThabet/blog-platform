package com.raid.blog.openapi.annotations.post;

import com.raid.blog.domain.dtos.ApiErrorResponse;
import com.raid.blog.domain.dtos.PostDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
        @ApiResponse(responseCode = "200", description = "Found posts with specified category and tag",
                content = {@Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))}),
        @ApiResponse(responseCode = "400", description = "Invalid request param",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )),
        @ApiResponse(responseCode = "404", description = "Category or tag not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "CategoryNotFound",
                                        summary = "When no category exists with the specified categoryId",
                                        value = "{\"status\":404,\"message\":\"Category not found\",\"errors\":null}"
                                ),
                                @ExampleObject(
                                        name = "TagNotFound",
                                        summary = "When no tag exists with the specified tagId",
                                        value = "{\"status\":404,\"message\":\"Tag not found\",\"errors\":null}"
                                )
                        }
                )),
})
public @interface SwaggerGetAllPostsResponses {
}
