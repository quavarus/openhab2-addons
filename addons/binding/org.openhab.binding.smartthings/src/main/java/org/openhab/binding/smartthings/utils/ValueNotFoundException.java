package org.openhab.binding.smartthings.utils;

/**
 * User: jhenry
 * Date: 3/31/2016
 * Time: 8:22 AM
 */
public class ValueNotFoundException extends RuntimeException {

    private static final String defaultMessage = "A value was not found where expected";

    public ValueNotFoundException() {
        super(defaultMessage);
    }

    public ValueNotFoundException(Throwable cause) {
        super(defaultMessage, cause);
    }

    public ValueNotFoundException(String message) {
        super(message);
    }

    public ValueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
