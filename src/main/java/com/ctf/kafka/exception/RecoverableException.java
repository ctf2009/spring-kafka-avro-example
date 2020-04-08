package com.ctf.kafka.exception;

public class RecoverableException extends RuntimeException {

    public RecoverableException(final String message) {
        super (message);
    }

}
