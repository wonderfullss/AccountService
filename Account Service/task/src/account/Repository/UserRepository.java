package account.Repository;

import account.Entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findUserByName(String name);

    User findUserByEmailIgnoreCase(String email);

    User findUserByEmail(String email);

    @Transactional
    void deleteUserByEmail(String email);

    List<User> findAllByOrderByIdAsc();
}