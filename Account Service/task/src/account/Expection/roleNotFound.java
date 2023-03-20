package account.Expection;

public class roleNotFound extends RuntimeException{
    public roleNotFound(String message){
        super(message);
    }
}
