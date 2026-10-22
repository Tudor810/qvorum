package com.qvorum.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown by services when a request cannot be completed. The message is shown to the user,
 * so it is written in Romanian.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mesaj) {
        super(mesaj);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
