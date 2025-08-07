package com.raid.blog.openapi.annotations.tag;

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
        @ApiResponse(responseCode = "200", description = "Created tags successfully without duplication",
                content = {@Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = PostDto.class)),
                        examples = @ExampleObject(
                                value = """
                                        [ {
                                          "id" : "8b70b9f5-3661-47c9-a1bd-3e607c9ce9bd",
                                          "name" : "Tag 2",
                                          "postCount" : 0
                                        }, {
                                          "id" : "7587bc2c-488d-476a-8380-3a9cbb7a1727",
                                          "name" : "Tag 1",
                                          "postCount" : 0
                                        } ]
                                """
                        ))}),
        @ApiResponse(responseCode = "400", description = "Invalid creation request",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "EmptyRequest",
                                        summary = "When the request is empty",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Validation failed",
                                                  "errors" : [ {
                                                    "field" : "names",
                                                    "message" : "At least one tag name is required"
                                                  } ]
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "InvalidTagsNames",
                                        summary = "When the provided names are not valid",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Validation failed",
                                                  "errors" : [ {
                                                    "field" : "names[]",
                                                    "message" : "Tag name must be between 2 and 30 characters"
                                                  }, {
                                                    "field" : "names[]",
                                                    "message" : "Tag name must be between 2 and 30 characters"
                                                  } ]
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "LongRequest",
                                        summary = "When the number of provided tags is more than 10",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Validation failed",
                                                  "errors" : [ {
                                                    "field" : "names",
                                                    "message" : "Maximum 10 tags allowed"
                                                  } ]
                                                }
                                                """
                                )
                        }
                ))
})
public @interface SwaggerCreateTagsResponses {
}
