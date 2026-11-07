package com.qvorum.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * One entry of the request history (UC25). Plain ids instead of relationships, so the history
 * stays intact when a user is deleted.
 */
@Entity
@Table(name = "request_logs")
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The client session (see ClientRegistry); null for requests sent without a valid token. */
    private String clientId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private RequestEventType eventType;

    private String method;
    private String path;

    /** HTTP status the server answered with. */
    private Integer status;

    private Integer durationMs;

    private LocalDateTime occurredAt;

    protected RequestLog() {
    }

    public RequestLog(String clientId, Long userId, RequestEventType eventType, String method, String path,
            Integer status, Integer durationMs) {
        this.clientId = clientId;
        this.userId = userId;
        this.eventType = eventType;
        this.method = method;
        this.path = path;
        this.status = status;
        this.durationMs = durationMs;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getClientId() { return clientId; }
    public Long getUserId() { return userId; }
    public RequestEventType getEventType() { return eventType; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Integer getStatus() { return status; }
    public Integer getDurationMs() { return durationMs; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
