package account.Controller;

import account.Repository.SecurityEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditorConroller {
    private final SecurityEventsRepository securityEventsRepository;

    @Autowired
    public AuditorConroller(SecurityEventsRepository securityEventsRepository) {
        this.securityEventsRepository = securityEventsRepository;
    }

    @GetMapping("/api/security/events")
    public ResponseEntity<?> getEvents() {
        return new ResponseEntity<>(securityEventsRepository.findAllByOrderByIdAsc(), HttpStatus.OK);
    }
}
