package com.raid.blog.openapi.annotations.category;

import com.raid.blog.domain.dtos.CategoryDto;
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
        @ApiResponse(responseCode = "200", description = "Found all categories",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CategoryDto.class),
                        examples = @ExampleObject(
                                value = """
                                        [ {
                                          "id" : "51d1fe26-ee49-4f01-9c39-38a22e0d7489",
                                          "name" : "Category 1",
                                          "postCount" : 1
                                        }, {
                                          "id" : "641a3b25-fcb2-46d9-be8c-809edc51bb1e",
                                          "name" : "Category 2",
                                          "postCount" : 0
                                        } ]
                                        """
                        )
                ))
})
public @interface SwaggerListCategoriesResponses {
}
