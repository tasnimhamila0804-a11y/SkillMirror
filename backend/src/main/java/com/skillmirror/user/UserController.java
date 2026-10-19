package com.skillmirror.user;

import com.skillmirror.auth.AuthService;
import com.skillmirror.auth.dto.AuthDtos.ChangePasswordRequest;
import com.skillmirror.auth.dto.AuthDtos.UpdateAccountRequest;
import com.skillmirror.security.AuthPrincipal;
import com.skillmirror.user.dto.UserDtos.UserSummary;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Account settings endpoints for the connected user. */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public UserSummary get(@AuthenticationPrincipal AuthPrincipal principal) {
        return UserSummary.from(authService.requireUser(principal.id()));
    }

    @PutMapping
    public UserSummary update(@AuthenticationPrincipal AuthPrincipal principal,
                              @Valid @RequestBody UpdateAccountRequest request) {
        return authService.updateAccount(principal.id(), request);
    }

    @PutMapping("/password")
    public org.springframework.http.ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.id(), request);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
