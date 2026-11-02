package com.qvorum.server.dto;

/** Matches AuthResponse in shared/src/types. */
public record LoginResponse(String token, UserDto user) {
}
