package com.schoolerp.student.exception;

public class CustomException extends RuntimeException {

    private final String errorCode;
    private final String details;

    public CustomException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
}

