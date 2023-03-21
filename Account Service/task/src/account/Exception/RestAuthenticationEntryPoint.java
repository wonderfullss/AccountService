package account.Exception;

import account.Entity.Role;
import account.Entity.SecurityEvents;
import account.Entity.User;
import account.Repository.SecurityEventsRepository;
import account.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityEventsRepository securityEventsRepository;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String username = new String(Base64.getDecoder().decode(authorization.split("\\s+")[1])).split(":")[0];
            String path = ServletUriComponentsBuilder.fromCurrentRequest().build().getPath();
            if (userRepository.findUserByEmailIgnoreCase(username) == null)
                securityEventsRepository.save(new SecurityEvents("LOGIN_FAILED", username.toLowerCase(), path, path));
            else {
                User user = userRepository.findUserByEmailIgnoreCase(username);
                if (user.isAccountNonLocked() && !user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
                    securityEventsRepository.save(new SecurityEvents("LOGIN_FAILED", username.toLowerCase(), path, path));
                    user.setCounter(user.getCounter() + 1);
                    userRepository.save(user);
                    if (user.getCounter() == 5) {
                        securityEventsRepository.save(new SecurityEvents("BRUTE_FORCE", username.toLowerCase(), path, path));
                        user.setAccountNonLocked(false);
                        securityEventsRepository.save(new SecurityEvents("LOCK_USER", username.toLowerCase(), String.format("Lock user %s", username), path));
                        userRepository.save(user);
                    }
                } else if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
                    securityEventsRepository.save(new SecurityEvents("LOGIN_FAILED", username.toLowerCase(), path, path));
                }
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
