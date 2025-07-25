package likelion.team1.mindscape.test.repository;

import likelion.team1.mindscape.test.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
	List<Test> findByUserIdOrderByCreatedAtDesc(Long userId);

	@Query("SELECT t.testId FROM Test t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
	List<Long> findTestIdsByUserId(@Param("userId") Long userId);

}

