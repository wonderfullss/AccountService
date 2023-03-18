package account.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentByUserDTO {
    String name;

    String lastname;

    String period;

    String salary;
}