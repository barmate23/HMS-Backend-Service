package com.schoolerp.student.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard response wrapper for all API responses
 * Provides consistent response structure across the application
 * 
 * @param <T> The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    
    /**
     * Indicates if the operation was successful
     */
    private boolean success;
    
    /**
     * Human-readable message describing the operation result
     */
    private String message;
    
    /**
     * The actual data payload
     */
    private T data;
    
    /**
     * Log ID for tracking this specific operation
     */
    private String logId;
    
    /**
     * Request ID for tracking the entire request
     */
    private String requestId;
    
    /**
     * Timestamp when the response was generated
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    
    /**
     * Error details (only present when success = false)
     */
    private ErrorDetails error;
    
    /**
     * Additional metadata about the operation
     */
    private ResponseMetadata metadata;
    
    /**
     * Error details nested class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private String message;
        private String details;
        private String field;
    }
    
    /**
     * Response metadata nested class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseMetadata {
        private Long totalRecords;
        private Integer currentPage;
        private Integer pageSize;
        private Integer totalPages;
        private Long executionTimeMs;
        private String operation;
    }
    
    /**
     * Creates a successful response with data
     * 
     * @param data The response data
     * @param message Success message
     * @param <T> Type of data
     * @return StandardResponse with success = true
     */
    public static <T> StandardResponse<T> success(T data, String message) {
        return StandardResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .logId(LogContext.getLogId())
            .requestId(LogContext.getRequestId())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Creates a successful response with data and metadata
     * 
     * @param data The response data
     * @param message Success message
     * @param metadata Response metadata
     * @param <T> Type of data
     * @return StandardResponse with success = true and metadata
     */
    public static <T> StandardResponse<T> success(T data, String message, ResponseMetadata metadata) {
        return StandardResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .logId(LogContext.getLogId())
            .requestId(LogContext.getRequestId())
            .timestamp(LocalDateTime.now())
            .metadata(metadata)
            .build();
    }
    
    /**
     * Creates a successful response with just a message
     * 
     * @param message Success message
     * @return StandardResponse with success = true
     */
    public static StandardResponse<Void> success(String message) {
        return StandardResponse.<Void>builder()
            .success(true)
            .message(message)
            .logId(LogContext.getLogId())
            .requestId(LogContext.getRequestId())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Creates an error response
     * 
     * @param message Error message
     * @param errorCode Error code
     * @param errorDetails Additional error details
     * @return StandardResponse with success = false
     */
    public static StandardResponse<Void> error(String message, String errorCode, String errorDetails) {
        return StandardResponse.<Void>builder()
            .success(false)
            .message(message)
            .logId(LogContext.getLogId())
            .requestId(LogContext.getRequestId())
            .timestamp(LocalDateTime.now())
            .error(ErrorDetails.builder()
                .code(errorCode)
                .message(message)
                .details(errorDetails)
                .build())
            .build();
    }
    
    /**
     * Creates an error response with field-specific error
     * 
     * @param message Error message
     * @param errorCode Error code
     * @param field Field that caused the error
     * @param errorDetails Additional error details
     * @return StandardResponse with success = false and field error
     */
    public static StandardResponse<Void> error(String message, String errorCode, String field, String errorDetails) {
        return StandardResponse.<Void>builder()
            .success(false)
            .message(message)
            .logId(LogContext.getLogId())
            .requestId(LogContext.getRequestId())
            .timestamp(LocalDateTime.now())
            .error(ErrorDetails.builder()
                .code(errorCode)
                .message(message)
                .field(field)
                .details(errorDetails)
                .build())
            .build();
    }
}