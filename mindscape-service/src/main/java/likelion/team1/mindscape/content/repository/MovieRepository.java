package likelion.team1.mindscape.content.repository;
import likelion.team1.mindscape.content.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitle(String title);
    List<Movie> findTop3AllByRecommendedContent_RecomId(Long recomId);
}