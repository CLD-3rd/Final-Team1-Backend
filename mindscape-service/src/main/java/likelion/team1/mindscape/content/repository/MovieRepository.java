package likelion.team1.mindscape.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import likelion.team1.mindscape.content.entity.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
}