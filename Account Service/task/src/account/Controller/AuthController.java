package account.Controller;

import account.Entity.*;
import account.Expection.EmailExistError;
import account.Expection.passwordError;
import account.Expection.paymentGetError;
import account.Expection.paymentsUpdateError;
import account.Repository.PaymentsRepository;
import account.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    private final PaymentsRepository paymentsRepository;

    private final Set<String> breachPass = Set.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder encoder, PaymentsRepository paymentsRepository) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.paymentsRepository = paymentsRepository;
    }

    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody User user) {
        if (userRepository.findUserByEmailIgnoreCase(user.getEmail()) == null) {
            if (breachPass.contains(user.getPassword()))
                throw new passwordError("The password is in the hacker's database!");
            else if (user.getPassword().length() < 12)
                throw new passwordError("The password length must be at least 12 chars!");
            else {
                user.setRole(Role.USER);
                user.setPassword(encoder.encode(user.getPassword()));
                userRepository.save(user);
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
        } else {
            throw new EmailExistError("User exist!");
        }
    }

    @PostMapping("/api/auth/changepass")
    public ResponseEntity<?> changePass(@RequestBody ChangePasswordDTO changePasswordDTO) {
        if (breachPass.contains(changePasswordDTO.getNewPassword()))
            throw new passwordError("The password is in the hacker's database!");
        if (changePasswordDTO.getNewPassword().length() < 12)
            throw new passwordError("Password length must be 12 chars minimum!");
        if (encoder.matches(changePasswordDTO.getNewPassword(), getCurrentUser().getPassword()))
            throw new passwordError("The passwords must be different!");
        getCurrentUser().setPassword(encoder.encode(changePasswordDTO.getNewPassword()));
        getCurrentUser().setEmail(getCurrentUser().getEmail().toLowerCase());
        userRepository.save(getCurrentUser());
        return new ResponseEntity<>(Map.of("email", getCurrentUser().getEmail(),
                "status", "The password has been updated successfully"),
                HttpStatus.OK);
    }

    @PostMapping("/api/acct/payments")
    public ResponseEntity<?> payments(@RequestBody @Valid List<Payments> payments) {
        List<Payments> temp = new ArrayList<>();
        for (Payments payment : payments) {
            if (paymentsRepository.findByPeriodAndEmployee(payment.getPeriod(), payment.getEmployee()) == null && !temp.contains(payment))
                temp.add(payment);
            else
                throw new paymentsUpdateError("rollback");
        }
        paymentsRepository.saveAll(temp);
        return new ResponseEntity<>(Map.of("status", "Added successfully!"), HttpStatus.OK);
    }

    @PutMapping("/api/acct/payments")
    public ResponseEntity<?> updatePayments(@RequestBody Payments payments) {
        if (paymentsRepository.findByPeriodAndEmployee(payments.getPeriod(), payments.getEmployee()) != null) {
            Payments temp = paymentsRepository.findByPeriodAndEmployee(payments.getPeriod(), payments.getEmployee());
            temp.setSalary(payments.getSalary());
            temp.setEmployee(payments.getEmployee());
            temp.setPeriod(payments.getPeriod());
            paymentsRepository.save(temp);
            return new ResponseEntity<>(Map.of("status", "Updated successfully!"), HttpStatus.OK);
        } else
            throw new paymentsUpdateError("No find");
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> payment(@RequestParam(required = false) String period) {
        if (period == null) {
            List<PaymentByUserDTO> paymentByUserDTOS = new ArrayList<>();
            List<Payments> payments = paymentsRepository.findPaymentsByEmployeeOrderByPeriodDesc(getCurrentUser().getEmail());
            for (Payments payment : payments) {
                YearMonth tempYear = YearMonth.parse(payment.getPeriod(), DateTimeFormatter.ofPattern("MM-yyyy"));
                String periodTemp = String.format("%s-%s".toLowerCase(), tempYear.getMonth(), tempYear.getYear());
                periodTemp = periodTemp.substring(0, 1).toUpperCase() + periodTemp.substring(1).toLowerCase();
                String salary = String.format("%d dollar(s) %d cent(s)", payment.getSalary() / 100, payment.getSalary() % 100);
                paymentByUserDTOS.add(new PaymentByUserDTO(getCurrentUser().getName(), getCurrentUser().getLastname(),
                        periodTemp, salary));
            }
            return new ResponseEntity<>(paymentByUserDTOS, HttpStatus.OK);
        } else {
            List<PaymentByUserDTO> paymentByUserDTOS = new ArrayList<>();
            List<Payments> payments = paymentsRepository.findPaymentsByEmployeeAndPeriodOrderByPeriodDesc(getCurrentUser().getEmail(), period);
            if(payments.size() == 0)
                throw new paymentGetError("Error");
            for (Payments payment : payments) {
                YearMonth tempYear = YearMonth.parse(payment.getPeriod(), DateTimeFormatter.ofPattern("MM-yyyy"));
                String periodTemp = String.format("%s-%s".toLowerCase(), tempYear.getMonth(), tempYear.getYear());
                periodTemp = periodTemp.substring(0, 1).toUpperCase() + periodTemp.substring(1).toLowerCase();
                String salary = String.format("%d dollar(s) %d cent(s)", payment.getSalary() / 100, payment.getSalary() % 100);
                paymentByUserDTOS.add(new PaymentByUserDTO(getCurrentUser().getName(), getCurrentUser().getLastname(),
                        periodTemp, salary));
            }
            return new ResponseEntity<>(paymentByUserDTOS, HttpStatus.OK);
        }
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}