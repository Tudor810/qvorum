package com.qvorum.server.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Development SMS sender: writes the message to the server log instead of sending it. */
@Service
public class LoggingSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsService.class);

    @Override
    public void send(String phoneNumber, String message) {
        log.info("SMS to {}: {}", phoneNumber, message);
    }
}
