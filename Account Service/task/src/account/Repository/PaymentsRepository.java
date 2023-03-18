package account.Repository;

import account.Entity.Payments;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentsRepository extends CrudRepository<Payments, Long> {
    Payments findByPeriodAndEmployee(String period, String employee);

    List<Payments> findPaymentsByEmployee(String employee);

    List<Payments> findPaymentsByEmployeeOrderByPeriodDesc(String employee);

    List<Payments> findPaymentsByEmployeeAndPeriodOrderByPeriodDesc(String employee, String period);
}