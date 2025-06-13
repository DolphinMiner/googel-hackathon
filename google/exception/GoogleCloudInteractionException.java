package com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.exception;

/**
 * @author white
 * created at 2025-06-10 10:16
 * GoogleCloudInteractionException
 * @version 1.0
 */
public class GoogleCloudInteractionException extends RuntimeException {

    public GoogleCloudInteractionException(String message) {
        super(message);
    }

    public GoogleCloudInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
