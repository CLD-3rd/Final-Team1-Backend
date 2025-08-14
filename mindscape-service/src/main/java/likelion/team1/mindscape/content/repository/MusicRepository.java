package likelion.team1.mindscape.content.repository;

import likelion.team1.mindscape.content.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    Optional<Music> findByTitleAndArtist(String title, String artist);

    List<Music> findTop3AllByRecommendedContent_RecomId(Long recommendedContentRecomId);

    @Query(value = "SELECT m.title, COUNT(*) as cnt, GROUP_CONCAT(DISTINCT m.music_id ORDER BY m.music_id ASC SEPARATOR ',') as music_ids " +
            "FROM music m WHERE m.recom_id IN :recomIds GROUP BY m.title ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> getMusicCountWithIds(List<Long> recomIds, int limit);

    Music getMusicByMusicId(Integer musicId);
}
