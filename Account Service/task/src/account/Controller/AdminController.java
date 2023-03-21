package account.Controller;

import account.Entity.*;
import account.Exception.deleteAdminError;
import account.Exception.emailNotFound;
import account.Exception.roleNotFound;
import account.Exception.userNoRole;
import account.Repository.SecurityEventsRepository;
import account.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class AdminController {

    private final UserRepository userRepository;
    private final SecurityEventsRepository securityEventsRepository;

    @Autowired
    public AdminController(UserRepository userRepository, SecurityEventsRepository securityEventsRepository) {
        this.userRepository = userRepository;
        this.securityEventsRepository = securityEventsRepository;
    }

    @GetMapping("/api/admin/user")
    public ResponseEntity<?> getUser() {
        return new ResponseEntity<>(userRepository.findAllByOrderByIdAsc(), HttpStatus.OK);
    }

    @PutMapping("/api/admin/user/access")
    public ResponseEntity<?> access(@RequestBody LockUserDTO lockUserDTO) {
        User user = userRepository.findUserByEmailIgnoreCase(lockUserDTO.getUser());
        if (user == null)
            throw new emailNotFound("User not found");
        if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
            throw new deleteAdminError("Can't lock the ADMINISTRATOR!");
        if (lockUserDTO.getOperation().equals("LOCK")) {
            user.setAccountNonLocked(false);
            userRepository.save(user);
            securityEventsRepository.save(new SecurityEvents("LOCK_USER", lockUserDTO.getUser(), String.format("Lock user %s", lockUserDTO.getUser()), "/api/admin/user/access"));
            return new ResponseEntity<>(Map.of("status", String.format("User %s locked!", lockUserDTO.getUser())), HttpStatus.OK);
        } else {
            user.setAccountNonLocked(true);
            userRepository.save(user);
            securityEventsRepository.save(new SecurityEvents("UNLOCK_USER", lockUserDTO.getUser(), String.format("Unlock user %s", lockUserDTO.getUser()), "/api/admin/user/access"));
            return new ResponseEntity<>(Map.of("status", String.format("User %s unlocked!", lockUserDTO.getUser())), HttpStatus.OK);
        }
    }

    @PutMapping("/api/admin/user/role")
    public ResponseEntity<?> updateRole(@RequestBody UpdateUserRoleDTO updateUserRoleDTO) {
        List<String> temp = List.of("ACCOUNTANT", "ADMINISTRATOR", "USER", "AUDITOR");
        if (userRepository.findUserByEmailIgnoreCase(updateUserRoleDTO.getUser()) == null)
            throw new emailNotFound("User not found!");
        if (!temp.contains(updateUserRoleDTO.getRole()))
            throw new roleNotFound("Role not found!");
        User user = userRepository.findUserByEmailIgnoreCase(updateUserRoleDTO.getUser());
        switch (updateUserRoleDTO.getOperation()) {
            case "REMOVE" -> {
                if (updateUserRoleDTO.getRole().equals("ADMINISTRATOR"))
                    throw new deleteAdminError("Can't remove ADMINISTRATOR role!");
                if (!user.getRoles().contains(Role.valueOf("ROLE_" + updateUserRoleDTO.getRole())))
                    throw new userNoRole("The user does not have a role!");
                if (user.getRoles().size() == 1)
                    throw new deleteAdminError("The user must have at least one role!");
                user.getRoles().remove(Role.valueOf("ROLE_" + updateUserRoleDTO.getRole()));
                Collections.sort(user.getRoles());
                userRepository.save(user);
                securityEventsRepository.save(new SecurityEvents("REMOVE_ROLE", getCurrentUser().getEmail().toLowerCase(), String.format("Remove role %s to %s", updateUserRoleDTO.getRole(), updateUserRoleDTO.getUser().toLowerCase()), "/api/admin/user/role"));
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
            case "GRANT" -> {
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR) && (updateUserRoleDTO.getRole().equals("ACCOUNTANT")
                        || updateUserRoleDTO.getRole().equals("USER") || updateUserRoleDTO.getRole().equals("AUDITOR")))
                    throw new userNoRole("The user cannot combine administrative and business roles!");
                else if ((user.getRoles().contains(Role.ROLE_ACCOUNTANT) || user.getRoles().contains(Role.ROLE_USER)
                        || user.getRoles().contains(Role.ROLE_AUDITOR)) && updateUserRoleDTO.getRole().equals("ADMINISTRATOR"))
                    throw new userNoRole("The user cannot combine administrative and business roles!");
                else {
                    user.getRoles().add(Role.valueOf("ROLE_" + updateUserRoleDTO.getRole()));
                    Collections.sort(user.getRoles());
                    userRepository.save(user);
                    securityEventsRepository.save(new SecurityEvents("GRANT_ROLE", getCurrentUser().getEmail().toLowerCase(), String.format("Grant role %s to %s", updateUserRoleDTO.getRole(), updateUserRoleDTO.getUser().toLowerCase()), "/api/admin/user/role"));
                    return new ResponseEntity<>(user, HttpStatus.OK);
                }
            }
            default -> {
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/api/admin/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        if (userRepository.findUserByEmailIgnoreCase(email) == null)
            throw new emailNotFound("User not found!");
        if (userRepository.findUserByEmailIgnoreCase(email).getRoles().contains(Role.ROLE_ADMINISTRATOR))
            throw new deleteAdminError("Can't remove ADMINISTRATOR role!");
        userRepository.deleteUserByEmail(email);
        securityEventsRepository.save(new SecurityEvents("DELETE_USER", getCurrentUser().getEmail(), email.toLowerCase(), "/api/admin/user"));
        return new ResponseEntity<>(Map.of("user", email, "status", "Deleted successfully!"), HttpStatus.OK);
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
