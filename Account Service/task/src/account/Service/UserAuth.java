package account.Service;

import account.Entity.User;
import account.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAuth implements UserDetailsService {
    private final UserRepository userRepository;

    public UserAuth(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmailIgnoreCase(email);
        if (user == null) {
            throw new UsernameNotFoundException("Not found: " + email);
        }
        return user;
    }
}
