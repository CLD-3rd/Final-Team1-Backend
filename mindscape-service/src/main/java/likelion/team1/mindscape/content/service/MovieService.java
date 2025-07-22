package likelion.team1.mindscape.content.service;

import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.TmdbResponse;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.entity.RecomContent;
import likelion.team1.mindscape.content.repository.MovieRepository;
import likelion.team1.mindscape.content.repository.RecomConentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovieService {

    @Value("${TMDB_API_KEY}")
    private String apiKey;
    private final RedisService redisService;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MovieRepository movieRepository;
    private final RecomConentRepository recomContentRepository;

    public List<MovieDto> getMovieInfo(String query){
        String url = "https://api.themoviedb.org/3/search/movie"
                + "?api_key=" + apiKey
                + "&query=" + UriUtils.encode(query, StandardCharsets.UTF_8)
                + "&language=ko&region=KR";
        TmdbResponse response = restTemplate.getForObject(url, TmdbResponse.class);
        return response != null ? response.getResults() : new ArrayList<>();
    }

    /**
     * Mysql: 영화 정보 저장
     * @param movieList
     * @param userId
     * @return
     */
    public Movie saveMovieToDB (List<MovieDto> movieList, Long userId){
        // 영화 조회
        if (movieList == null || movieList.isEmpty()) {
            throw new IllegalArgumentException("movie list is empty(DB)");
        }
        MovieDto dto = movieList.get(0);
        RecomContent recom = recomContentRepository.findLatestByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("추천 결과가 없습니다."));
        // 1) 기존 영화: recomId만 업뎃
        Optional<Movie> existing = movieRepository.findByTitle(dto.getTitle());
        if(existing.isPresent()){
            Movie movie = existing.get();
            movie.setRecommendedContent(recom);
            return movieRepository.save(movie);
        }
        // 2) 새로운 영화: 모두 저장
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPoster("http://image.tmdb.org/t/p/w500"+dto.getPoster());
        movie.setRecommendedContent(recom);
        return movieRepository.save(movie);
    }
    /**
     * Redis 영화 저장
     *
     */
    public void saveMovieToRedis(List<MovieDto> movieList){
        if(movieList == null || movieList.isEmpty()){
            throw new IllegalArgumentException("movie list is empty(Redis)");
        }
        MovieDto dto = movieList.get(0);
        String searchPattern = "movie:*:title:"+dto.getTitle();
        Set<String> keys = redisTemplate.keys(searchPattern);
        if (keys != null && !keys.isEmpty()) {
            System.out.println(dto.getTitle() + ": redis에 이미 존재");
            return;
        }
        // redis 저장
        Long id = redisService.MovieToRedis(dto);
        System.out.println(dto.getTitle() + ": redis에 저장 완료 (id=" + id + ")");
    }
}
