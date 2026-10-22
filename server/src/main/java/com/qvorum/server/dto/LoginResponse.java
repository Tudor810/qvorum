package com.qvorum.server.dto;

/** Matches RaspunsAutentificare in shared/src/types. */
public record LoginResponse(String token, UtilizatorDto utilizator) {
}
