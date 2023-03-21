package account.Exception;

public class EmailExistError extends RuntimeException {
    public EmailExistError(String message) {
        super(message);
    }
}