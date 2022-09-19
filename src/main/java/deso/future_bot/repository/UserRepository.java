package deso.future_bot.repository;

import deso.future_bot.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data SQL repository for the Load entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findOneByLoginOrPhoneNumber(String lowercaseLogin, String lowercaseLogin1);
}
