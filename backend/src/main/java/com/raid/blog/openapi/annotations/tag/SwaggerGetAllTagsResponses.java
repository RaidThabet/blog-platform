package com.raid.blog.openapi.annotations.tag;

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
                                          "id" : "8b70b9f5-3661-47c9-a1bd-3e607c9ce9bd",
                                          "name" : "Tag 2",
                                          "postCount" : 0
                                        }, {
                                          "id" : "7587bc2c-488d-476a-8380-3a9cbb7a1727",
                                          "name" : "Tag 1",
                                          "postCount" : 0
                                        } ]
                                """
                        ))})
        ,
})
public @interface SwaggerGetAllTagsResponses {
}
