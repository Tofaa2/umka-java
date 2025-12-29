package me.tofaa.umka.error;

public class UmkaException extends RuntimeException {

    public UmkaException() {
    }

    public UmkaException(String message) {
        super(message);
    }

    public UmkaException(String message, Throwable cause) {
        super(message, cause);
    }

    public UmkaException(Throwable cause) {
        super(cause);
    }

    public UmkaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
