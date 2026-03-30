package se.edugrade.java25.enterprise.gym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.edugrade.java25.enterprise.gym.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
