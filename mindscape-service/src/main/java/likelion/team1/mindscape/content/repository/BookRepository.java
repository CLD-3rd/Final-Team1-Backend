package likelion.team1.mindscape.content.repository;

import likelion.team1.mindscape.content.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
