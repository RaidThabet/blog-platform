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
                        schema = @Schema(implementation = PostDto.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "id" : "a37927f0-305e-4923-ac6c-4999b8eef71c",
                                          "title" : "Title 1",
                                          "content" : "This is some testing content text",
                                          "author" : {
                                            "id" : "bc191238-263a-443c-8052-06a7ef45022a",
                                            "name" : "Raid"
                                          },
                                          "category" : {
                                            "id" : "45b5240b-90e0-451a-81bb-59d8cf37cf56",
                                            "name" : "Category 1",
                                            "postCount" : 0
                                          },
                                          "tags" : [ {
                                            "id" : "5c118ddb-030b-464a-8772-6d5f924eef88",
                                            "name" : "Tag 1",
                                            "postCount" : null
                                          } ],
                                          "readingTime" : 1,
                                          "createdAt" : "2025-08-05T20:04:19.771401",
                                          "updatedAt" : "2025-08-05T20:04:19.771424",
                                          "status" : "PUBLISHED"
                                        }
                                        """
                        )
                )}),
        @ApiResponse(responseCode = "400", description = "Invalid creation request",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "status" : 400,
                                          "message" : "Validation failed",
                                          "errors" : [ {
                                            "field" : "content",
                                            "message" : "Content is required"
                                          }, {
                                            "field" : "content",
                                            "message" : "Content must be between 10 and 50000 characters"
                                          }, {
                                            "field" : "title",
                                            "message" : "Title must be between 3 and 200 characters"
                                          }, {
                                            "field" : "title",
                                            "message" : "Title is required"
                                          } ]
                                        }
                                        """
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
