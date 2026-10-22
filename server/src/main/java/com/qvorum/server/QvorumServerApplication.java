package com.qvorum.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point. Starts the HTTP server and the administrator console. */
@SpringBootApplication
public class QvorumServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(QvorumServerApplication.class, args);
    }
}
