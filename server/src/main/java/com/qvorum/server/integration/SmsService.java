package com.qvorum.server.integration;

/**
 * Sends SMS messages. The requirements ask for email + SMS when login details change. The course
 * version uses LoggingSmsService; a real provider (e.g. Twilio) would be one more class
 * implementing this interface, and no caller would change.
 */
public interface SmsService {

    void send(String phoneNumber, String message);
}
