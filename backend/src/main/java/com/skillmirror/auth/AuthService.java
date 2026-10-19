package com.skillmirror.auth;

import com.skillmirror.admin.SettingsService;
import com.skillmirror.auth.dto.AuthDtos.AuthResponse;
import com.skillmirror.auth.dto.AuthDtos.ChangePasswordRequest;
import com.skillmirror.auth.dto.AuthDtos.LoginRequest;
import com.skillmirror.auth.dto.AuthDtos.RegisterRequest;
import com.skillmirror.auth.dto.AuthDtos.UpdateAccountRequest;
import com.skillmirror.common.ApiException;
import com.skillmirror.profile.Profile;
import com.skillmirror.profile.ProfileRepository;
import com.skillmirror.security.JwtService;
import com.skillmirror.security.LoginRateLimiter;
import com.skillmirror.user.User;
import com.skillmirror.user.UserRepository;
import com.skillmirror.user.UserRole;
import com.skillmirror.user.dto.UserDtos.UserSummary;
import java.time.Instant;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registration, login and account management use cases. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter rateLimiter;
    private final SettingsService settingsService;

    public AuthService(UserRepository userRepository, ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, LoginRateLimiter rateLimiter,
                       SettingsService settingsService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
        this.settingsService = settingsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "Un compte existe déjà avec cet e-mail.");
        }
        if (!settingsService.isRegistrationEnabled()) {
            throw ApiException.forbidden("La création de comptes est actuellement désactivée.");
        }
        validatePasswordStrength(request.password());

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setProfileCompleted(false);
        user = userRepository.save(user);

        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profileRepository.save(profile);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, UserSummary.from(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientKey) {
        String email = normalizeEmail(request.email());
        if (!rateLimiter.allow(email)) {
            throw ApiException.tooManyRequests("TOO_MANY_ATTEMPTS",
                    "Trop de tentatives. Réessayez dans quelques minutes.");
        }
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            rateLimiter.recordFailure(clientKey == null ? email : clientKey);
            throw ApiException.unauthorized("E-mail ou mot de passe incorrect.");
        }
        if (!user.isEnabled()) {
            throw ApiException.forbidden("Ce compte a été désactivé.");
        }
        rateLimiter.reset(email);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, UserSummary.from(user));
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable."));
    }

    @Transactional
    public UserSummary updateAccount(Long userId, UpdateAccountRequest request) {
        User user = requireUser(userId);
        user.setName(request.name().trim());
        user.touch();
        return UserSummary.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("INVALID_CURRENT_PASSWORD", "Mot de passe actuel incorrect.");
        }
        validatePasswordStrength(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.touch();
        userRepository.save(user);
    }

    private void validatePasswordStrength(String password) {
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        if (!hasLetter || !hasDigit) {
            throw ApiException.badRequest("WEAK_PASSWORD",
                    "Le mot de passe doit contenir au moins une lettre et un chiffre.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
