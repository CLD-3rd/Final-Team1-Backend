package likelion.team1.mindscape.content.repository;

import likelion.team1.mindscape.content.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitle(String title);

    List<Book> findTop3AllByRecommendedContent_RecomId(Long recommendedContentRecomId);
}