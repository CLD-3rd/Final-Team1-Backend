package likelion.team1.mindscape.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import likelion.team1.mindscape.content.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // recomContent 기준 조회 등도 확장 가능
}