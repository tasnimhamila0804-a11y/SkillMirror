package com.skillmirror.security;

/** Authenticated principal injected in controllers via @AuthenticationPrincipal. */
public record AuthPrincipal(Long id, String email, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
