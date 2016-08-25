package org.openhab.binding.smartthings.utils;

/**
 * User: jhenry
 * Date: 3/31/2016
 * Time: 8:20 AM
 */
public class ValueNotUniqueException extends RuntimeException {

    private static final String defaultMessage = "A unique value was not found where expected";

    public ValueNotUniqueException() {
        super(defaultMessage);
    }

    public ValueNotUniqueException(Throwable cause) {
        super(defaultMessage, cause);
    }

    public ValueNotUniqueException(String message) {
        super(message);
    }

    public ValueNotUniqueException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueNotUniqueException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
