package account.Expection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class forbiddenError extends RuntimeException {
    public forbiddenError(String message) {
        super(message);
    }
}
