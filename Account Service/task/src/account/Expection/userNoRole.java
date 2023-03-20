package account.Expection;

public class userNoRole extends RuntimeException {
    public userNoRole(String message) {
        super(message);
    }
}
