package com.qvorum.server.monitoring;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * monitoring: state observed by the system administrator: connected clients, connection and
 * disconnection events, and the request/response history (UC32, UC33).
 */
@Component
public class ClientRegistry {

    public record ClientInfo(String id, String address, Instant connectedAt) {
    }

    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public void register(ClientInfo client) {
        clients.put(client.id(), client);
    }

    public void unregister(String clientId) {
        clients.remove(clientId);
    }

    public Collection<ClientInfo> list() {
        return clients.values();
    }
}
