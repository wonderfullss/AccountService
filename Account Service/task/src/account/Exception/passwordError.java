package account.Exception;

public class passwordError extends RuntimeException {
    public passwordError(String message) {
        super(message);
    }
}