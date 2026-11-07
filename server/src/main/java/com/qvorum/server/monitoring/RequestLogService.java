package com.qvorum.server.monitoring;

import com.qvorum.server.model.RequestEventType;
import com.qvorum.server.model.RequestLog;
import com.qvorum.server.repository.RequestLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Saves the request history (UC25) in the request_logs table. */
@Service
public class RequestLogService {

    private static final Logger log = LoggerFactory.getLogger(RequestLogService.class);

    private final RequestLogRepository repository;

    public RequestLogService(RequestLogRepository repository) {
        this.repository = repository;
    }

    public void recordRequest(String clientId, Long userId, String method, String path, int status, long durationMs) {
        save(new RequestLog(clientId, userId, RequestEventType.REQUEST, method, truncate(path), status,
                (int) Math.min(durationMs, Integer.MAX_VALUE)));
    }

    public void recordEvent(String clientId, Long userId, RequestEventType eventType) {
        save(new RequestLog(clientId, userId, eventType, null, null, null, null));
    }

    /** The history must never break the request it describes, so a failure is only logged. */
    private void save(RequestLog entry) {
        try {
            repository.save(entry);
        } catch (RuntimeException e) {
            log.warn("Could not save the request history entry", e);
        }
    }

    /** The column holds 255 characters. */
    private static String truncate(String path) {
        return path.length() <= 255 ? path : path.substring(0, 255);
    }
}
