package com.qvorum.server.console;

import com.qvorum.server.monitoring.ClientRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * console: the system administrator's text interface (UC23-UC25). Starts once the server is
 * ready and reads commands from standard input on its own thread, so HTTP requests keep being
 * served while it waits for input.
 */
@Component
public class AdminConsole {

    private final ClientRegistry clientRegistry;
    private final ApplicationContext context;
    private final Instant startedAt = Instant.now();

    public AdminConsole(ClientRegistry clientRegistry, ApplicationContext context) {
        this.clientRegistry = clientRegistry;
        this.context = context;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread thread = new Thread(this::readCommands, "admin-console");
        thread.setDaemon(true);
        thread.start();
    }

    private void readCommands() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Qvorum server console. Type 'help' for commands.");
        while (scanner.hasNextLine()) {
            String[] words = scanner.nextLine().trim().split("\\s+");
            switch (words[0]) {
                case "help" -> System.out.println("status | clients | disconnect <client id> | exit");
                case "status" -> System.out.println("Running for " + Duration.between(startedAt, Instant.now()).toSeconds() + " s");
                case "clients" -> printClients();
                case "disconnect" -> disconnect(words);
                case "exit" -> System.exit(SpringApplication.exit(context));
                case "" -> { }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void printClients() {
        var clients = clientRegistry.list();
        System.out.println(clients.size() + " connected client(s)");
        for (ClientRegistry.ClientInfo c : clients) {
            System.out.printf("%s  %-30s %-15s connected %s, last seen %s%n",
                    c.id(), c.email(), c.address(), c.connectedAt().withNano(0), c.lastSeenAt().withNano(0));
        }
    }

    private void disconnect(String[] words) {
        if (words.length != 2) {
            System.out.println("Usage: disconnect <client id>  (see 'clients')");
            return;
        }
        clientRegistry.forceDisconnect(words[1]).ifPresentOrElse(
                c -> System.out.println("Disconnected " + c.email() + ". Their next request will be refused."),
                () -> System.out.println("No connected client with id " + words[1]));
    }
}
