package likelion.team1.mindscape.content.service;
import likelion.team1.mindscape.content.dto.response.content.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public Long MovieToRedis(MovieDto dto) {
        Long newId = redisTemplate.opsForValue().increment("movie:id");
        String key = "movie:" +dto.getTitle();
        // date format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String releaseDateStr = formatter.format(dto.getReleaseDate());
        // saved as hash
        Map<String, String> movieMap = new HashMap<>();
        movieMap.put("title", dto.getTitle());
        movieMap.put("description", dto.getDescription());
        movieMap.put("poster", "http://image.tmdb.org/t/p/w500" + dto.getPoster());
        movieMap.put("release_date", releaseDateStr);

        redisTemplate.opsForHash().putAll(key, movieMap);
        return newId;
    }

    public Long MusicToRedis(MusicDto dto) {
        Long newId = redisTemplate.opsForValue().increment("music:id");
        // saved as hash
        String key = "music:" + dto.getTitle();

        Map<String, String> musicMap = new HashMap<>();
        musicMap.put("title", dto.getTitle());
        musicMap.put("artist", dto.getArtist());
        musicMap.put("album", dto.getAlbum());

        // duplication check
        Map<Object, Object> existingMap = redisTemplate.opsForHash().entries(key);
        boolean isDifferent = existingMap.isEmpty() || !musicMap.equals(existingMap);

        if (isDifferent) {
            redisTemplate.opsForHash().putAll(key, musicMap);
        }
        return newId;
    }

    public Long BookToRedis(BookDto dto){
        Long newId = redisTemplate.opsForValue().increment("book:id");
        String key = "book:" + dto.getTitle();

        Map<String, String> bookMap = new HashMap<>();
        bookMap.put("title", dto.getTitle());
        bookMap.put("author", dto.getAuthor());
        bookMap.put("description", dto.getDescription());
        bookMap.put("image", dto.getImage());

        redisTemplate.opsForHash().putAll(key, bookMap);
        return newId;
    }

    public BookResponse getAlternativeBook(List<String> excludeTitles) {
        Set<String> keys = redisTemplate.keys("book:*");

        if (keys.isEmpty() || keys == null) {
            return null;
        }
        for (String key : keys) {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            String title = map.get("title").toString();
            if (title == null || excludeTitles.contains(title)) {
                continue;
            }
            return new BookResponse(
                    (String) map.get("title"),
                    (String) map.get("author"),
                    (String) map.get("description"),
                    (String) map.get("image")
            );
        }
        return null;
    }

    public MusicResponse getAlternativeMusic(List<String> excludeTitles) {
        Set<String> keys = redisTemplate.keys("mjsic:*");

        if (keys.isEmpty() || keys == null) {
            return null;
        }
        for (String key : keys) {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            String title = map.get("title").toString();
            if (title == null || excludeTitles.contains(title)) {
                continue;
            }
            return new MusicResponse(
                    (String) map.get("title"),
                    (String) map.get("artist"),
                    (String) map.get("album")
            );
        }
        return null;
    }

    public String makeRecomKey(Long userId, Long testId, String contentType) {
        return String.format("user:%d:test:%d:%s", userId, testId, contentType);
    }
}
