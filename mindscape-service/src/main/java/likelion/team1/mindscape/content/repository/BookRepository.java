package likelion.team1.mindscape.content.repository;

import likelion.team1.mindscape.content.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitle(String title);

    List<Book> findTop3AllByRecommendedContent_RecomId(Long recommendedContentRecomId);

    @Query(value = "SELECT b.title, COUNT(*) as cnt, GROUP_CONCAT(DISTINCT b.book_id ORDER BY b.book_id ASC SEPARATOR ',') as book_ids " +
            "FROM book b WHERE b.recom_id IN :recomIds GROUP BY b.title ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> getBookCountWithIds(List<Long> recomIds, int limit);

    Book getBookByBookId(Long bookId);
}