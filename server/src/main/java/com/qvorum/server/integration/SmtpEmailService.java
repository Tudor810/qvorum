package com.qvorum.server.integration;

import com.qvorum.server.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends emails through an SMTP server (spring.mail.* in application.properties). In development
 * that server is Mailpit from docker-compose.yml: it accepts every email and shows it at
 * http://localhost:8025 instead of delivering it, so no real inbox is ever reached.
 */
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailService(JavaMailSender mailSender, @Value("${qvorum.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Could not send email to {}", to, e);
            // 502 Bad Gateway: an external service failed (docs/api-contract.md, section 1.6).
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "The email could not be sent. Please try again later.");
        }
    }
}
