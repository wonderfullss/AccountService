package account.Expection;

public class paymentGetError extends RuntimeException {
    public paymentGetError(String message) {
        super(message);
    }
}