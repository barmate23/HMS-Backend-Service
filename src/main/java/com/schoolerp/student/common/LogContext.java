package com.schoolerp.student.common;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for managing log context throughout the application
 * Provides consistent logId tracking across all service methods
 */
@UtilityClass
public class LogContext {
    
    private static final String LOG_ID_KEY = "logId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String OPERATION_KEY = "operation";
    
    /**
     * Generates and sets a new logId in the current thread context
     * 
     * @return Generated logId
     */
    public static String generateLogId() {
        String logId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MDC.put(LOG_ID_KEY, logId);
        return logId;
    }
    
    /**
     * Sets logId in the current thread context
     * 
     * @param logId The logId to set
     */
    public static void setLogId(String logId) {
        MDC.put(LOG_ID_KEY, logId != null ? logId : generateLogId());
    }
    
    /**
     * Gets the current logId from thread context
     * 
     * @return Current logId or generates new one if not set
     */
    public static String getLogId() {
        String logId = MDC.get(LOG_ID_KEY);
        return logId != null ? logId : generateLogId();
    }
    
    /**
     * Sets request ID for tracking API requests
     * 
     * @param requestId The request ID
     */
    public static void setRequestId(String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }
    
    /**
     * Sets user ID for audit tracking
     * 
     * @param userId The user ID
     */
    public static void setUserId(String userId) {
        MDC.put(USER_ID_KEY, userId);
    }
    
    /**
     * Sets operation name for tracking
     * 
     * @param operation The operation name
     */
    public static void setOperation(String operation) {
        MDC.put(OPERATION_KEY, operation);
    }
    
    /**
     * Gets current request ID
     * 
     * @return Current request ID
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }
    
    /**
     * Gets current user ID
     * 
     * @return Current user ID
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }
    
    /**
     * Gets current operation
     * 
     * @return Current operation
     */
    public static String getOperation() {
        return MDC.get(OPERATION_KEY);
    }
    
    /**
     * Clears all context from current thread
     */
    public static void clear() {
        MDC.clear();
    }
    
    /**
     * Clears specific key from context
     * 
     * @param key The key to clear
     */
    public static void clear(String key) {
        MDC.remove(key);
    }
}