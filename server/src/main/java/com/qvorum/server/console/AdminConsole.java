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
            switch (scanner.nextLine().trim()) {
                case "help" -> System.out.println("status | clients | exit");
                case "status" -> System.out.println("Running for " + Duration.between(startedAt, Instant.now()).toSeconds() + " s");
                case "clients" -> System.out.println(clientRegistry.list());
                case "exit" -> System.exit(SpringApplication.exit(context));
                case "" -> { }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }
}
