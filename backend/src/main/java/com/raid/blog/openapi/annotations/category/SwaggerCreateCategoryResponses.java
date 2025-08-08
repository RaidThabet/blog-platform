package com.raid.blog.openapi.annotations.category;

import com.raid.blog.domain.dtos.ApiErrorResponse;
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
        @ApiResponse(responseCode = "201", description = "Created new post for authenticated user",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = CategoryDto.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                           "id" : "780c70f0-7ca2-4729-8f3d-8856e28cbf1d",
                                           "name" : "New Category",
                                           "postCount" : 0
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
                                            "field" : "name",
                                            "message" : "Category name is required"
                                          }, {
                                            "field" : "name",
                                            "message" : "Category name must be between 2 and 50 characters"
                                          } ]
                                        }
                                        """
                        )
                )),
        @ApiResponse(responseCode = "409", description = "Category with the specified name already exists",
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
                ))})
public @interface SwaggerCreateCategoryResponses {
}
