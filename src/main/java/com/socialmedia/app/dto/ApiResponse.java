package com.socialmedia.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean status;          // true = success, false = error
    private int statusCode;          // HTTP status code
    private String message;          // Description or error message
    private T data;                  // Any data payload
    private Instant timestamp;       // Current timestamp

    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .status(true)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .status(false)
                .message(message)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .build();
    }
}
