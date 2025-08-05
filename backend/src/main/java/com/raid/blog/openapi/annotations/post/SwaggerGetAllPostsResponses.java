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
                        array = @ArraySchema(schema = @Schema(implementation = PostDto.class)),
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
                                  "status" : "PUBLISHED"
                                }, {
                                  "id" : "f4e36843-8367-43fe-8745-09e39a88bae2",
                                  "title" : "Title 2",
                                  "content" : "This is some testing content text",
                                  "author" : {
                                    "id" : "b89db9a8-27ac-4556-a220-cc9bb612462b",
                                    "name" : "Raid"
                                  },
                                  "category" : {
                                    "id" : "d2315d61-151a-4758-ac2e-24901c7650bd",
                                    "name" : "Category 2",
                                    "postCount" : 0
                                  },
                                  "tags" : [ {
                                    "id" : "2d884d78-b755-4d80-ae6e-6be0def828e0",
                                    "name" : "Tag 2",
                                    "postCount" : null
                                  } ],
                                  "readingTime" : 1,
                                  "createdAt" : "2025-08-05T19:49:06.190593",
                                  "updatedAt" : "2025-08-05T19:49:06.190611",
                                  "status" : "PUBLISHED"
                                } ]
                                """
                ))}),
        @ApiResponse(responseCode = "400", description = "Invalid request param",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "CategporyIdInvalid",
                                        summary = "When the categoryId param is not a valid UUID",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Invalid value provided for parameter 'categoryId'. Expected type: 'UUID'.",
                                                  "errors" : null
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "TagIdInvalid",
                                        summary = "When the tagId param is not a valid UUID",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Invalid value provided for parameter 'tagId'. Expected type: 'UUID'.",
                                                  "errors" : null
                                                }
                                                """
                                )
                        }
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
