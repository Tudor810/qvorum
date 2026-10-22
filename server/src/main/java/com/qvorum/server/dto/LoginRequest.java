package com.qvorum.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * dto: objects sent to and received from the clients, serialized as JSON.
 * Field names must match the types in shared/src/types on the client side.
 * The annotations are checked when the controller parameter is marked with @Valid.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String parola) {
}
