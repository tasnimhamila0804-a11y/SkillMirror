package com.skillmirror.common;

import com.skillmirror.security.AuthPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Persists technical errors for admin supervision. Never throws: logging must not break requests. */
@Service
public class ErrorLogService {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogService.class);

    private final ErrorLogRepository repository;

    public ErrorLogService(ErrorLogRepository repository) {
        this.repository = repository;
    }

    public void record(String level, String message, HttpServletRequest request, Throwable cause) {
        try {
            ErrorLog entry = new ErrorLog();
            entry.setLevel(level);
            entry.setMessage(truncate(message, 1000));
            if (request != null) {
                entry.setPath(truncate(request.getRequestURI(), 300));
                entry.setMethod(request.getMethod());
                entry.setUserId(currentUserId(request));
            }
            if (cause != null) {
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement el : cause.getStackTrace()) {
                    sb.append(el.toString()).append('\n');
                    if (sb.length() > 700) break;
                }
                entry.setDetails(truncate(sb.toString(), 4000));
            }
            repository.save(entry);
        } catch (Exception ex) {
            log.warn("Unable to persist error log: {}", ex.getMessage());
        }
    }

    public void record(String level, String message) {
        record(level, message, null, null);
    }

    public void recordFromNow(String level, String message, String path, String method, Throwable cause) {
        try {
            ErrorLog entry = new ErrorLog();
            entry.setLevel(level);
            entry.setMessage(truncate(message, 1000));
            entry.setPath(truncate(path, 300));
            entry.setMethod(method);
            entry.setCreatedAt(Instant.now());
            if (cause != null) {
                entry.setDetails(truncate(String.valueOf(cause), 4000));
            }
            repository.save(entry);
        } catch (Exception ex) {
            log.warn("Unable to persist error log: {}", ex.getMessage());
        }
    }

    private Long currentUserId(HttpServletRequest request) {
        try {
            Authentication auth = (Authentication) request.getUserPrincipal();
            if (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal) {
                return principal.id();
            }
        } catch (Exception ignored) {
            // best effort only
        }
        return null;
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
