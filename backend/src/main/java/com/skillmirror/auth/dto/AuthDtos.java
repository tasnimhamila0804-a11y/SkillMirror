package com.skillmirror.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request/response payloads of the authentication API. */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "Le nom est requis.")
            @Size(max = 120, message = "Le nom est trop long.")
            String name,

            @NotBlank(message = "L'e-mail est requis.")
            @Email(message = "Format d'e-mail invalide.")
            @Size(max = 190)
            String email,

            @NotBlank(message = "Le mot de passe est requis.")
            @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
            String password) {}

    public record LoginRequest(
            @NotBlank(message = "L'e-mail est requis.")
            @Email(message = "Format d'e-mail invalide.")
            String email,

            @NotBlank(message = "Le mot de passe est requis.")
            String password) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "Le mot de passe actuel est requis.")
            String currentPassword,

            @NotBlank(message = "Le nouveau mot de passe est requis.")
            @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
            String newPassword) {}

    public record UpdateAccountRequest(
            @NotBlank(message = "Le nom est requis.")
            @Size(max = 120)
            String name) {}

    public record AuthResponse(String token, com.skillmirror.user.dto.UserDtos.UserSummary user) {}
}
