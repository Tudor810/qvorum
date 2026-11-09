package com.qvorum.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * dto: objects sent to and received from the clients, serialized as JSON.
 * Field names must match the types in shared/src/types on the client side.
 * The annotations are checked when the controller parameter is marked with @Valid; their
 * "message" is shown to the user next to the field (the "fields" part of a 400 response).
 */
public record LoginRequest(
        @NotBlank(message = "Enter your email address.")
        @Email(message = "Enter a valid email address.")
        String email,

        @NotBlank(message = "Enter your password.")
        String password) {
}
