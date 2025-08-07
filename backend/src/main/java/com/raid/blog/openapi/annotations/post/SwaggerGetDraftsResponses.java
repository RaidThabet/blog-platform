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
        @ApiResponse(responseCode = "200", description = "Found draft posts that belong to the authenticated user",
                content = {@Content(
                        mediaType = "application/json",
                        array = @ArraySchema(
                                schema = @Schema(implementation = PostDto.class)
                        ),
                        examples = @ExampleObject(
                                value = """
                                [ {
                                  "id" : "cee0ef62-1ba1-46a8-8093-b0d3aa4b48ff",
                                  "title" : "Title 1",
                                  "content" : "This is some testing content text",
                                  "author" : {
                                    "id" : "b89db9a8-27ac-4556-a220-cc9bb612462b",
                                    "name" : "Raid"
                                  },
                                  "category" : {
                                    "id" : "db28c9c6-0263-4143-8d70-d263e0f13352",
                                    "name" : "Category 1",
                                    "postCount" : 0
                                  },
                                  "tags" : [ {
                                    "id" : "4d72f6ba-c473-4858-9387-13ec157390a8",
                                    "name" : "Tag 1",
                                    "postCount" : null
                                  } ],
                                  "readingTime" : 1,
                                  "createdAt" : "2025-08-05T19:49:06.153099",
                                  "updatedAt" : "2025-08-05T19:49:06.153122",
                                  "status" : "DRAFT"
                                  },
                                ]
                                """
                        ))}),
        @ApiResponse(responseCode = "400", description = "Invalid authenticated user",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"status\":400,\"message\":\"Invalid value provided for parameter 'id'. Expected type: 'UUID'.\",\"errors\":null}"
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
public @interface SwaggerGetDraftsResponses {
}
