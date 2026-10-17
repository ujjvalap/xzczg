package com.neuromart.exception;

import org.springframework.http.HttpStatus;

public class HandleError extends RuntimeException {
    private final HttpStatus status;
    private final Object details;

    public HandleError(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.details = null;
    }

    public HandleError(String message, HttpStatus status, Object details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public HandleError(String message, int statusCode) {
        super(message);
        this.status = HttpStatus.valueOf(statusCode);
        this.details = null;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }
}
