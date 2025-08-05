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
        @ApiResponse(responseCode = "200", description = "Updated post for authenticated user",
                content = {@Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PostDto.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                            "id":"1aa559f4-e620-42ff-b463-e553c8bea670",
                                            "title":"New Title 1",
                                            "content":"New content for post 1",
                                            "author":{
                                                "id":"408f0d0c-74c8-4c40-975d-155737ad8242",
                                                "name":"Raid"
                                            },
                                            "category":{
                                                "id":"9d4a03a4-6ee2-4ec5-bc78-ff13a94e81a1",
                                                "name":"Category 2",
                                                "postCount":0
                                            },
                                            "tags":[],
                                            "readingTime":1,
                                            "createdAt":"2025-08-05T16:32:13.513907",
                                            "updatedAt":"2025-08-05T16:32:13.838084",
                                            "status":"DRAFT"
                                        }
                                        """
                        )
                ),}),
        @ApiResponse(responseCode = "400", description = "Invalid update request",
                content = @Content),
        @ApiResponse(responseCode = "404", description = "Post with specified id not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"status\":404,\"message\":\"Post does not exist\",\"errors\":null}"
                        )
                ))})
public @interface SwaggerUpdatePostResponses {
}
