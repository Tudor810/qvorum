package com.qvorum.server.model;

/** What a row of request_logs records (UC25). */
public enum RequestEventType {
    /** A client logged in and received a token. */
    CONNECT,
    /** A client logged out, or the system administrator disconnected it. */
    DISCONNECT,
    /** Any other HTTP request, with the status the server answered. */
    REQUEST
}
