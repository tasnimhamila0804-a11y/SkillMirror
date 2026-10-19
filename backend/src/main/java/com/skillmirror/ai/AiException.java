package com.skillmirror.ai;

/** Failure of an external AI provider (timeout, quota, malformed answer...). */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
