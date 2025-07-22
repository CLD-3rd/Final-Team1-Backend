package likelion.team1.mindscape.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import likelion.team1.mindscape.content.entity.Music;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
}