package likelion.team1.mindscape.content.repository;

import likelion.team1.mindscape.content.entity.RecomContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecomContentRepository extends JpaRepository<RecomContent, Long> {
    /*
    TODO: 임의로 mj_test 테이블 생성함 -:> test로 변경!
     */
    @Query(value = "SELECT rc.* FROM recommended_content rc " +
            "JOIN mj_test t ON rc.test_id = t.testId " +
            "WHERE t.user_id = :userId " +
            "ORDER BY t.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<RecomContent> findLatestByUserId(@Param("userId") Long userId);
}
