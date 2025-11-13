# Logging and Response Structure Guide

## 📋 Overview

The Student Microservice implements comprehensive **logId tracking** and **standardized response formats** throughout all service methods and API endpoints. This ensures consistent logging, debugging, and API response structures.

## 🔍 LogId Implementation Pattern

### LogId Generation and Context
```java
// Automatic logId generation for every HTTP request
String logId = LogContext.getLogId();  // Gets current thread's logId

// LogId is automatically added to all log statements
log.info("logId: {} - Starting operation for student: {}", logId, studentId);
```

### Service Method Pattern
All service methods follow this consistent pattern:

```java
@Override
public StudentResponseDto getStudentById(String id) {
    String logId = LogContext.getLogId();
    log.info("logId: {} - Starting getStudentById for id: {}", logId, id);
    
    try {
        // Business logic here
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("logId: {} - Student not found with id: {}", logId, id);
                return new ResourceNotFoundException("Student not found with id: " + id);
            });
        
        log.info("logId: {} - Successfully retrieved student: {} {}", 
                 logId, student.getFirstName(), student.getLastName());
        
        return studentMapper.toDto(student);
        
    } catch (Exception e) {
        log.error("logId: {} - Error retrieving student with id {}: {}", 
                  logId, id, e.getMessage(), e);
        throw e;
    }
}
```

## 📊 Standard Response Format

### Response Structure
All API responses follow the `StandardResponse<T>` wrapper pattern:

```json
{
  "success": true,
  "message": "Students retrieved successfully",
  "data": {
    "students": [...],
    "total": 25
  },
  "logId": "A1B2C3D4",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-26T19:30:15.123Z",
  "metadata": {
    "totalRecords": 25,
    "currentPage": 0,
    "pageSize": 20,
    "operation": "GET_ALL_STUDENTS"
  }
}
```

### Success Response Types

#### 1. Success with Data
```java
StandardResponse.success(data, "Operation successful", metadata)
```

#### 2. Success without Data
```java
StandardResponse.success("Student deleted successfully")
```

#### 3. Success with Pagination Metadata
```java
StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
    .totalRecords(response.getTotal())
    .currentPage(page)
    .pageSize(size)
    .operation("GET_ALL_STUDENTS")
    .build();
    
StandardResponse.success(data, "Students retrieved successfully", metadata)
```

### Error Response Format
```json
{
  "success": false,
  "message": "Student not found with id: 123",
  "logId": "A1B2C3D4",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-01-26T19:30:15.123Z",
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Student not found with id: 123",
    "details": "The requested resource was not found in the system"
  }
}
```

## 🔧 Logging Architecture Components

### 1. LogContext Utility
```java
public class LogContext {
    // Generate new logId for operation
    public static String generateLogId()
    
    // Set/Get logId in thread context
    public static void setLogId(String logId)
    public static String getLogId()
    
    // Set/Get request ID for full request tracking
    public static void setRequestId(String requestId)
    public static String getRequestId()
    
    // Set operation name
    public static void setOperation(String operation)
    
    // Clear thread context
    public static void clear()
}
```

### 2. Logging Interceptor
Automatically sets up logging context for all HTTP requests:

```java
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate requestId and logId
        String requestId = UUID.randomUUID().toString();
        String logId = LogContext.generateLogId();
        
        // Set context
        LogContext.setRequestId(requestId);
        LogContext.setOperation(request.getMethod() + " " + request.getRequestURI());
        
        // Add tracking headers to response
        response.setHeader("X-Request-ID", requestId);
        response.setHeader("X-Log-ID", logId);
    }
}
```

### 3. Enhanced Log Pattern
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{logId:-}] [%X{requestId:-}] [%X{operation:-}] %-5level %logger{36} - %msg%n"
```

**Sample Log Output:**
```
2025-01-26 19:30:15 [A1B2C3D4] [550e8400-e29b-41d4-a716-446655440000] [POST /api/students] INFO  c.s.s.service.StudentService - logId: A1B2C3D4 - Starting createStudent for: John Doe
```

## 🎯 Controller Pattern Implementation

### API Endpoint Pattern
```java
@PostMapping
public ResponseEntity<StandardResponse<StudentResponseDto>> createStudent(@Valid @RequestBody StudentRequestDto requestDto) {
    String logId = LogContext.getLogId();
    log.info("logId: {} - API call: POST /api/students for: {} {}", 
             logId, requestDto.getFirstName(), requestDto.getLastName());
    
    StudentResponseDto createdStudent = studentService.createStudent(requestDto);
    
    StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
            .operation("CREATE_STUDENT")
            .build();
    
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(StandardResponse.success(createdStudent, "Student created successfully", metadata));
}
```

## 📈 Operation Tracking

### Supported Operations
- `GET_ALL_STUDENTS` - List students with filtering
- `GET_STUDENT_BY_ID` - Retrieve single student
- `CREATE_STUDENT` - Create new student
- `UPDATE_STUDENT` - Update existing student
- `DELETE_STUDENT` - Delete student
- `PROMOTE_STUDENTS` - Bulk student promotion
- `CREATE_ID_CARD` - Issue ID card
- `CREATE_MARKSHEET` - Generate marksheet
- `CREATE_DOCUMENT` - Upload document

### Metadata Tracking
```java
StandardResponse.ResponseMetadata.builder()
    .totalRecords(count)           // Total records affected/returned
    .currentPage(page)             // Current page (for pagination)
    .pageSize(size)                // Page size (for pagination)
    .operation("OPERATION_NAME")   // Operation identifier
    .executionTimeMs(duration)     // Operation duration (future enhancement)
    .build()
```

## 🛡️ Error Handling with LogId

### Exception Handler Pattern
```java
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
```

### Error Codes
- `RESOURCE_NOT_FOUND` - Entity not found
- `VALIDATION_FAILED` - Input validation error
- `INTERNAL_SERVER_ERROR` - Unexpected system error

## 🔄 Request Flow with LogId Tracking

1. **HTTP Request** → Interceptor generates `requestId` and `logId`
2. **Controller** → Logs API call with `logId`
3. **Service Layer** → All operations use same `logId`
4. **Repository** → Database operations tracked with `logId`
5. **Response** → Includes `logId` and `requestId` for client tracking
6. **Error Handling** → All errors include `logId` for debugging

## 📊 Monitoring and Debugging Benefits

### Request Tracing
- Track complete request lifecycle with single `requestId`
- Track individual operations with `logId`
- Correlate logs across service boundaries

### Performance Monitoring
- Measure operation duration
- Identify slow operations
- Track success/failure rates

### Error Investigation
- Quick error identification with `logId`
- Complete error context with request details
- Structured error responses for API clients

### Client Integration
- Response headers include tracking IDs for client debugging
- Consistent error format for client error handling
- Metadata for pagination and operation details

This implementation provides enterprise-grade logging and response standardization that ensures complete traceability and consistent API behavior across all student management operations.