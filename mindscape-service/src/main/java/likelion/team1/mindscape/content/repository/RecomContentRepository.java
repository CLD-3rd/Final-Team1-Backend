package likelion.team1.mindscape.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import likelion.team1.mindscape.content.entity.RecomContent;

@Repository
public interface RecomContentRepository extends JpaRepository<RecomContent, Long> {
    // recomId (PK) 기반 조회 가능
}
