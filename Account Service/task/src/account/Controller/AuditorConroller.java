package account.Controller;

import account.Entity.User;
import account.Repository.SecurityEventsRepository;
import account.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditorConroller {
    private final SecurityEventsRepository securityEventsRepository;

    private final UserRepository userRepository;

    @Autowired
    public AuditorConroller(SecurityEventsRepository securityEventsRepository, UserRepository userRepository) {
        this.securityEventsRepository = securityEventsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/security/events")
    public ResponseEntity<?> getEvents() {
        getCurrentUser().setCounter(0);
        userRepository.save(getCurrentUser());
        return new ResponseEntity<>(securityEventsRepository.findAllByOrderByIdAsc(), HttpStatus.OK);
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
