package com.schoolerp.student.interceptor;

import com.schoolerp.student.common.LogContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor to set up logging context for all HTTP requests
 * Automatically generates logId and requestId for tracking
 */
@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String LOG_ID_HEADER = "X-Log-ID";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate or extract request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Generate logId for this operation
        String logId = LogContext.generateLogId();
        
        // Set context
        LogContext.setRequestId(requestId);
        LogContext.setOperation(request.getMethod() + " " + request.getRequestURI());
        
        // Add headers to response for client tracking
        response.setHeader(REQUEST_ID_HEADER, requestId);
        response.setHeader(LOG_ID_HEADER, logId);
        
        // Log the incoming request
        log.info("Incoming request: {} {} | logId: {} | requestId: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                logId, 
                requestId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // Log the response
        log.info("Request completed: {} {} | Status: {} | logId: {} | requestId: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                response.getStatus(),
                LogContext.getLogId(),
                LogContext.getRequestId());
        
        // Clear context to prevent memory leaks
        LogContext.clear();
    }
}