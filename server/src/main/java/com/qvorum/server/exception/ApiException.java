package com.qvorum.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown by services when a request cannot be completed. The message is shown to the user
 * as is, so it must be a complete, readable sentence.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
