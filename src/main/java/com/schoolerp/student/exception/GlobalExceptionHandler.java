package com.schoolerp.student.exception;

import com.schoolerp.student.common.LogContext;
import com.schoolerp.student.common.StandardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String logId = LogContext.getLogId();
        log.error("logId: {} - Resource not found: {}", logId, ex.getMessage());
        
        StandardResponse<Void> response = StandardResponse.error(
            ex.getMessage(), 
            "RESOURCE_NOT_FOUND", 
            "The requested resource was not found in the system"
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String logId = LogContext.getLogId();
        log.error("logId: {} - Validation error: {}", logId, ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        StandardResponse<Map<String, String>> response = StandardResponse.<Map<String, String>>builder()
            .success(false)
            .message("Validation failed")
            .data(validationErrors)
            .logId(logId)
            .requestId(LogContext.getRequestId())
            .timestamp(LocalDateTime.now())
            .error(StandardResponse.ErrorDetails.builder()
                .code("VALIDATION_FAILED")
                .message("Input validation failed")
                .details("One or more fields have invalid values")
                .build())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Void>> handleGenericException(Exception ex) {
        String logId = LogContext.getLogId();
        log.error("logId: {} - Unexpected error: ", logId, ex);
        
        StandardResponse<Void> response = StandardResponse.error(
            "An unexpected error occurred", 
            "INTERNAL_SERVER_ERROR", 
            "Please contact support if this issue persists. LogID: " + logId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<StandardResponse<Void>> handleCustomException(CustomException ex) {
        String logId = LogContext.getLogId();
        log.error("logId: {} - Custom exception: {}", logId, ex.getMessage());

        StandardResponse<Void> response = StandardResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .logId(logId)
            .requestId(LogContext.getRequestId())
            .timestamp(LocalDateTime.now())
            .error(StandardResponse.ErrorDetails.builder()
                .code(ex.getErrorCode() != null ? ex.getErrorCode() : "CUSTOM_ERROR")
                .message(ex.getMessage())
                .details(ex.getDetails() != null ? ex.getDetails() : ("Please contact support if this issue persists. LogID: " + logId))
                .build())
            .build();

        return ResponseEntity.badRequest().body(response);
    }
}