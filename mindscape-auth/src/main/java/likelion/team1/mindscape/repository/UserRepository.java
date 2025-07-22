package likelion.team1.mindscape.repository;

import likelion.team1.mindscape.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
}
