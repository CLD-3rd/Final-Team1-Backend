package likelion.team1.mindscape.test.repository;

import likelion.team1.mindscape.test.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
	List<Test> findByUserIdOrderByCreatedAtDesc(Long userId);
}

