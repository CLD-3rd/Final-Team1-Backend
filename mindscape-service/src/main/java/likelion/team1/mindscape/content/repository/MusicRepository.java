package likelion.team1.mindscape.content.repository;

import likelion.team1.mindscape.content.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    Optional<Music> findByTitleAndArtist(String title, String artist);

    List<Music> findTop3AllByRecommendedContent_RecomId(Long recommendedContentRecomId);
}
