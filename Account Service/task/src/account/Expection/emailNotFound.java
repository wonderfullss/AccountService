package account.Expection;

public class emailNotFound extends RuntimeException{
    public emailNotFound(String message){
        super(message);
    }
}
