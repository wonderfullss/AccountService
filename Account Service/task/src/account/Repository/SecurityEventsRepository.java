package account.Repository;

import account.Entity.SecurityEvents;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventsRepository extends CrudRepository<SecurityEvents, Long> {
    List<SecurityEvents> findAllByOrderByIdAsc();
}
