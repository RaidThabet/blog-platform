package com.raid.blog.openapi.annotations.auth;

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
@ApiResponses({
        @ApiResponse(
                responseCode = "202", description = "When a new account is successfully created",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "400", description = "When the request is invalid",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "EmptyBody",
                                        description = "When the request body is missing or empty",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Required request body is missing",
                                                  "errors" : null
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "InvalidRequest",
                                        description = "When the request fields do not pass validation",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Validation failed",
                                                  "errors" : [ {
                                                    "field" : "name",
                                                    "message" : "Name cannot be blank"
                                                  }, {
                                                    "field" : "password",
                                                    "message" : "Password cannot be empty"
                                                  }, {
                                                    "field" : "email",
                                                    "message" : "Email cannot be blank"
                                                  }, {
                                                    "field" : "name",
                                                    "message" : "Name cannot be empty"
                                                  }, {
                                                    "field" : "email",
                                                    "message" : "Email cannot be empty"
                                                  }, {
                                                    "field" : "password",
                                                    "message" : "Password cannot be blank"
                                                  } ]
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "409", description = "When attempting to create an already existing account",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "status" : 409,
                                          "message" : "User with this email already exists",
                                          "errors" : null
                                        }
                                        """
                        )
                )
        )
})
public @interface SwaggerRegisterResponses {
}
