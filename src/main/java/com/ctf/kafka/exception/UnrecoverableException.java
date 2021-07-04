package com.ctf.kafka.exception;

public class UnrecoverableException extends RuntimeException {

    public UnrecoverableException(final String message) {
        super(message);
    }

    public UnrecoverableException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
