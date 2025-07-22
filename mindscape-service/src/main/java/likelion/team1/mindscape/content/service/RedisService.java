package likelion.team1.mindscape.content.service;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
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
        String key = "movie:" + newId+":title:"+dto.getTitle();
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

    public Map<Object, Object> getMovieByIdAndTitle(Long id, String title) {
        String key = "movie:" + id + ":title:" + title;
        return redisTemplate.opsForHash().entries(key);
    }
    private String buildKey(String title, String contentType){
        return String.format("%s:%s", contentType, title);
    }
}
