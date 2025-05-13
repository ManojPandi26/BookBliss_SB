package com.BookBliss.DTO.Common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Need To Implement
/**
 * Generic response wrapper for all API responses
 * @param <T> Type of data returned in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    /**
     * Indicates if the operation was successful
     */
    private boolean success;

    /**
     * Optional response message
     */
    private String message;

    /**
     * Data payload
     */
    private T data;

    /**
     * Error code if applicable
     */
    private String errorCode;

    /**
     * Timestamp of the response
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Static factory method for successful responses
     *
     * @param data The response data
     * @param message Success message
     * @return ApiResponseDto with success=true and the provided data
     * @param <T> Type of data
     */
    public static <T> ApiResponseDto<T> success(T data, String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Static factory method for error responses
     *
     * @param message Error message
     * @param errorCode Optional error code
     * @return ApiResponseDto with success=false
     * @param <T> Type of data (typically Void for errors)
     */
    public static <T> ApiResponseDto<T> error(String message, String errorCode) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
