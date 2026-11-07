package com.qvorum.server.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exists only in tests: an endpoint restricted to one role, to check that @PreAuthorize works. */
@RestController
class RoleProbeController {

    @GetMapping("/test/admin-only")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    String adminOnly() {
        return "ok";
    }
}
