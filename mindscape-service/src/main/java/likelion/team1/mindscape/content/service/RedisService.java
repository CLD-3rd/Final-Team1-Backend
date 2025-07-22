package likelion.team1.mindscape.content.service;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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
    public Map<Object, Object> getMovieByIdAndTitle(Long id, String title) {
        String key = "movie:" + title;
        return redisTemplate.opsForHash().entries(key);
    }
    private String buildKey(String title, String contentType){
        return String.format("%s:%s", contentType, title);
    }
}
