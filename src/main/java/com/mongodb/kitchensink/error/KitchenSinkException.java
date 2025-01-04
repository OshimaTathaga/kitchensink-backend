package com.mongodb.kitchensink.error;

public class KitchenSinkException extends RuntimeException {
    public KitchenSinkException(String message) {
        super(message);
    }

    public KitchenSinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public KitchenSinkException(Throwable cause) {
        super(cause);
    }
}
