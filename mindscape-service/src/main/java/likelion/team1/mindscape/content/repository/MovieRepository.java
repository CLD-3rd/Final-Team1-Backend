package likelion.team1.mindscape.content.repository;

import likelion.team1.mindscape.content.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitle(String title);

    List<Movie> findTop3AllByRecommendedContent_RecomId(Long recomId);

    @Query(value = "SELECT m.title, COUNT(*) as cnt, GROUP_CONCAT(DISTINCT m.movie_id ORDER BY m.movie_id ASC SEPARATOR ',') as movie_ids " +
            "FROM movie m WHERE m.recom_id IN :recomIds GROUP BY m.title ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> getMovieCountWithIds(List<Long> recomIds, int limit);

    Movie getMovieByMovieId(Long movieId);
}