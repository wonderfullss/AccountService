package account.Expection;

public class paymentsUpdateError extends RuntimeException {
    public paymentsUpdateError(String message) {
        super(message);
    }
}