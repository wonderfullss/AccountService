package account.Controller;

import account.Entity.Role;
import account.Entity.UpdateUserRoleDTO;
import account.Entity.User;
import account.Expection.deleteAdminError;
import account.Expection.emailNotFound;
import account.Expection.roleNotFound;
import account.Expection.userNoRole;
import account.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class AdminController {

    private final UserRepository userRepository;

    @Autowired
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/admin/user")
    public ResponseEntity<?> getUser() {
        return new ResponseEntity<>(userRepository.findAllByOrderByIdAsc(), HttpStatus.OK);
    }

    @PutMapping("/api/admin/user/role")
    public ResponseEntity<?> updateRole(@RequestBody UpdateUserRoleDTO updateUserRoleDTO) {
        List<String> temp = List.of("ACCOUNTANT", "ADMINISTRATOR", "USER");
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
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
            case "GRANT" -> {
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR) && updateUserRoleDTO.getRole().equals("ACCOUNTANT")
                        || updateUserRoleDTO.getRole().equals("USER"))
                    throw new userNoRole("The user cannot combine administrative and business roles!");
                if (user.getRoles().contains(Role.ROLE_ACCOUNTANT) || user.getRoles().contains(Role.ROLE_USER) && updateUserRoleDTO.getRole().equals("ADMINISTRATOR"))
                    throw new userNoRole("The user cannot combine administrative and business roles!");
                user.getRoles().add(Role.valueOf("ROLE_" + updateUserRoleDTO.getRole()));
                Collections.sort(user.getRoles());
                userRepository.save(user);
                return new ResponseEntity<>(user, HttpStatus.OK);
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
        return new ResponseEntity<>(Map.of("user", email, "status", "Deleted successfully!"), HttpStatus.OK);
    }
}
