package com.raid.blog.openapi.annotations.category;

import com.raid.blog.domain.dtos.ApiErrorResponse;
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
        @ApiResponse(responseCode = "204", description = "Deleted user post"),
        @ApiResponse(responseCode = "409", description = "Cannot delete a category with posts associated to it",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiErrorResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                  "status" : 409,
                                                  "message" : "Category already exists with name Category 1",
                                                  "errors" : null
                                                }
                                                """
                                )
                        ))
        }
)
public @interface SwaggerDeleteCategoryResponses {
}
