package com.qvorum.server.monitoring;

import com.qvorum.server.model.RequestEventType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * monitoring: state observed by the system administrator: connected clients, connection and
 * disconnection events, and the request/response history (UC24, UC25).
 *
 * A "client" is one logged-in app: every login creates a client session with its own id, which is
 * stored inside the token. A token is accepted only while its session is in this registry, so
 * logging out or a forced disconnect makes the token useless immediately. The registry lives in
 * memory, so restarting the server disconnects everyone.
 */
@Component
public class ClientRegistry {

    public record ClientInfo(String id, Long userId, String email, String address,
            LocalDateTime connectedAt, LocalDateTime lastSeenAt) {
    }

    /** How long a forced disconnect is remembered: as long as the token could still be valid. */
    private static final Duration CLOSED_MEMORY = Duration.ofHours(24);

    /** ConcurrentHashMap: HTTP requests run on many threads at once, and the console on another. */
    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    /**
     * Sessions the system administrator closed, with the time it happened. Kept so the client can
     * be told why its token stopped working ("closed by the administrator" instead of "log in").
     */
    private final Map<String, LocalDateTime> closedByAdministrator = new ConcurrentHashMap<>();

    private final RequestLogService requestLogService;

    public ClientRegistry(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    /** Registers a new client session after a successful login and returns it. */
    public ClientInfo connect(Long userId, String email, String address) {
        LocalDateTime now = LocalDateTime.now();
        ClientInfo client = new ClientInfo(UUID.randomUUID().toString(), userId, email, address, now, now);
        clients.put(client.id(), client);
        requestLogService.recordEvent(client.id(), userId, RequestEventType.CONNECT);
        return client;
    }

    /** Ends a session (logout or forced disconnect). Returns the removed client, if it existed. */
    public Optional<ClientInfo> disconnect(String clientId) {
        ClientInfo removed = clients.remove(clientId);
        if (removed != null) {
            requestLogService.recordEvent(removed.id(), removed.userId(), RequestEventType.DISCONNECT);
        }
        return Optional.ofNullable(removed);
    }

    /** The system administrator closes a session from the console (UC24). */
    public Optional<ClientInfo> forceDisconnect(String clientId) {
        Optional<ClientInfo> removed = disconnect(clientId);
        removed.ifPresent(c -> {
            LocalDateTime now = LocalDateTime.now();
            closedByAdministrator.values().removeIf(closedAt -> closedAt.isBefore(now.minus(CLOSED_MEMORY)));
            closedByAdministrator.put(c.id(), now);
        });
        return removed;
    }

    /** True if this session ended because the system administrator disconnected it. */
    public boolean wasClosedByAdministrator(String clientId) {
        return clientId != null && closedByAdministrator.containsKey(clientId);
    }

    public boolean isConnected(String clientId) {
        return clientId != null && clients.containsKey(clientId);
    }

    /** Records that the client has just sent a request. */
    public void touch(String clientId) {
        clients.computeIfPresent(clientId, (id, c) ->
                new ClientInfo(c.id(), c.userId(), c.email(), c.address(), c.connectedAt(), LocalDateTime.now()));
    }

    /** The connected clients, oldest connection first. */
    public List<ClientInfo> list() {
        return clients.values().stream()
                .sorted(Comparator.comparing(ClientInfo::connectedAt))
                .toList();
    }
}
