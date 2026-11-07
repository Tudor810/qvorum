package com.qvorum.server.integration;

/**
 * integration: services that talk to the outside world (email, SMS, later Stripe).
 *
 * Sends emails: temporary passwords, email confirmation links, messages to service companies.
 * The rest of the code depends only on this interface, never on how emails are delivered
 * (SmtpEmailService today), so the delivery can change without touching any caller and tests can
 * replace it with a fake.
 */
public interface EmailService {

    /**
     * Sends a plain-text email.
     *
     * @throws com.qvorum.server.exception.ApiException with status 503 if the email cannot be sent
     */
    void send(String to, String subject, String body);
}
