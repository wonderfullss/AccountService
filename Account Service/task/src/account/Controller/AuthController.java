package account.Controller;

import account.Entity.Role;
import account.Entity.User;
import account.Expection.EmailExistError;
import account.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody User user) {
        if (userRepository.findUserByEmailIgnoreCase(user.getEmail()) == null) {
            user.setRole(Role.USER);
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }else{
            throw new EmailExistError("User exist!");
        }
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> payment() {
        return new ResponseEntity<>(getCurrentUser(), HttpStatus.OK);
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
