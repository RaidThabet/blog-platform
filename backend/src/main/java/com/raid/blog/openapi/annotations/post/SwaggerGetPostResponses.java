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
        @ApiResponse(responseCode = "200", description = "Found post with specified id",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PostDto.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "id" : "7f8188c8-b123-4cb8-a7ff-262187307129",
                                          "title" : "Title 1",
                                          "content" : "This is some testing content text",
                                          "author" : {
                                            "id" : "e2312b47-5902-4b70-8497-d637bd291ee6",
                                            "name" : "Raid"
                                          },
                                          "category" : {
                                            "id" : "aab2e516-c9e1-445c-8f5c-4a08dd893382",
                                            "name" : "Category 1",
                                            "postCount" : 0
                                          },
                                          "tags" : [ {
                                            "id" : "693269e8-7ccf-400d-aa61-c639d3658967",
                                            "name" : "Tag 1",
                                            "postCount" : null
                                          } ],
                                          "readingTime" : 1,
                                          "createdAt" : "2025-08-07T15:07:37.833726",
                                          "updatedAt" : "2025-08-07T15:07:37.833769",
                                          "status" : "PUBLISHED"
                                        }
                                        """
                        )
                )),
        @ApiResponse(responseCode = "400", description = "Invalid id",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"status\":400,\"message\":\"Invalid value provided for parameter 'id'. Expected type: 'UUID'.\",\"errors\":null}"
                        )
                )),
        @ApiResponse(responseCode = "404", description = "Post with the specified id does not exist",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"status\":404,\"message\":\"Post does not exist\",\"errors\":null}"
                        )
                )),
})
public @interface SwaggerGetPostResponses {
}
