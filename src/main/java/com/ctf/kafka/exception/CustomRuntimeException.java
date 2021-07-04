package com.ctf.kafka.exception;

public class CustomRuntimeException extends RuntimeException {

    public CustomRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
