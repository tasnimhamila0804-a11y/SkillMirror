package com.skillmirror.user.dto;

import com.skillmirror.user.User;
import java.time.Instant;

/** Shared user DTOs returned by the API. */
public final class UserDtos {

    private UserDtos() {}

    public record UserSummary(Long id, String name, String email, String role, boolean profileCompleted,
                              Instant createdAt) {
        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole().name(),
                    user.isProfileCompleted(), user.getCreatedAt());
        }
    }
}
