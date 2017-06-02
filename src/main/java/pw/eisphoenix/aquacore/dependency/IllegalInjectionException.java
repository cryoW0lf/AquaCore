package pw.eisphoenix.aquacore.dependency;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class IllegalInjectionException extends RuntimeException {

    public IllegalInjectionException() {
    }

    public IllegalInjectionException(String message) {
        super(message);
    }

    public IllegalInjectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalInjectionException(Throwable cause) {
        super(cause);
    }

    public IllegalInjectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
