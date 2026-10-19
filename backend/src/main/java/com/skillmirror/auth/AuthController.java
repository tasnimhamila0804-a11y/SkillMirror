package com.skillmirror.auth;

import com.skillmirror.auth.dto.AuthDtos.AuthResponse;
import com.skillmirror.auth.dto.AuthDtos.LoginRequest;
import com.skillmirror.auth.dto.AuthDtos.RegisterRequest;
import com.skillmirror.security.AuthPrincipal;
import com.skillmirror.user.dto.UserDtos.UserSummary;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String clientKey = clientKey(http);
        return authService.login(request, clientKey);
    }

    /** Stateless JWT: the client discards its token; the server simply confirms the logout. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserSummary me(@AuthenticationPrincipal AuthPrincipal principal) {
        return UserSummary.from(authService.requireUser(principal.id()));
    }

    private String clientKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String forward = request.getHeader("X-Forwarded-For");
        if (forward != null && !forward.isBlank()) {
            ip = forward.split(",")[0].trim();
        }
        return ip;
    }
}
