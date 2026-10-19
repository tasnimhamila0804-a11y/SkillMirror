package com.skillmirror.security;

import com.skillmirror.user.User;
import com.skillmirror.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Extracts the Bearer token from the Authorization header and populates the security context. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            AuthPrincipal principal = jwtService.parse(header.substring(7).trim());
            if (principal != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findById(principal.id()).orElse(null);
                if (user != null && user.isEnabled()) {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                    var authentication = new UsernamePasswordAuthenticationToken(
                            new AuthPrincipal(user.getId(), user.getEmail(), user.getRole().name()),
                            null,
                            List.of(authority));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
