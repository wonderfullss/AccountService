package account.Expection;

public class deleteAdminError extends RuntimeException{
    public deleteAdminError(String message){
        super(message);
    }
}
