package com.raid.blog.openapi.annotations.auth;

import com.raid.blog.domain.dtos.ApiErrorResponse;
import com.raid.blog.domain.dtos.AuthResponse;
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
                responseCode = "200", description = "User authenticated successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "token" : "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJyYWlkQG1haWwuY29tIiwiaWF0IjoxNzU0OTQ3MTg2LCJleHAiOjE3NTUwMzM1ODZ9.hxksjbIN7cdhlE0YPUwOIQpbQFbYhoslFX1B17k__NT0VQOg2xOmmB6iTo7rBD2J",
                                          "expiresIn" : 86400
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401", description = "No user with such credentials exists",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "status" : 401,
                                          "message" : "Incorrect username or password",
                                          "errors" : null
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400", description = "When the request is not valid",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "NoRequestBody",
                                        description = "When no body is provided in the request",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Required request body is missing",
                                                  "errors" : null
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "InvalidCredentials",
                                        description = "When the credentials are not valid",
                                        value = """
                                                {
                                                  "status" : 400,
                                                  "message" : "Validation failed",
                                                  "errors" : [ {
                                                    "field" : "email",
                                                    "message" : "Email is invalid"
                                                  }, {
                                                    "field" : "password",
                                                    "message" : "Password should contain at least 8 characters"
                                                  } ]
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface SwaggerAuthenticateResponses {
}
