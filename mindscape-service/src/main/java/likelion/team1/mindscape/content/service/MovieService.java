package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MovieResponse;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.entity.RecomContent;
import likelion.team1.mindscape.content.repository.MovieRepository;
import likelion.team1.mindscape.content.repository.RecomContentRepository;
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
    private final RecomContentRepository recomContentRepository;

    public List<Movie> updateMovieFromTitle(Long userId, Long recomId) {
        // 1. movie 검색
        List<Movie> targetMovies = movieRepository.findByRecommendedContent_RecomId(recomId);
        List<Movie> updatedList = new ArrayList<>();
        for (Movie movie : targetMovies) {
            if (hasCompleteInfo(movie)) {
                updateRecomOnly(movie, userId);
                updatedList.add(movieRepository.save(movie));
            } else {
                List<MovieDto> movieInfo = getMovieInfo(movie.getTitle());
                Movie updated = createNewMovie(movie, movieInfo.get(0), userId);
                updatedList.add(updated);
                saveMovieToRedis(movieInfo);
            }
        }
        return updatedList;
    }

    public List<MovieDto> getMovieInfo(String query){
        String url = "https://api.themoviedb.org/3/search/movie"
                + "?api_key=" + apiKey
                + "&query=" + UriUtils.encode(query, StandardCharsets.UTF_8)
                + "&language=ko&region=KR";
        MovieResponse response = restTemplate.getForObject(url, MovieResponse.class);
        return response != null ? response.getResults() : new ArrayList<>();
    }

    /**
     * Mysql: 영화 정보 저장
     * movie -> title, desc.. 정보 모두 있음: updateRecomOnly, 정보 없음: createNewMovie
     *
     */
    private boolean hasCompleteInfo(Movie movie) {
        return movie.getDescription() != null && movie.getReleaseDate() != null && movie.getPoster() != null;
    }
    private void updateRecomOnly(Movie movie, Long userId) {
        RecomContent recom = getLatestRecom(userId);
        movie.setRecommendedContent(recom);
    }
    private RecomContent getLatestRecom(Long userId) {
        return recomContentRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("추천 결과가 없습니다."));
    }

    private Movie createNewMovie(Movie movie, MovieDto dto, Long userId){
        movie.setDescription(dto.getDescription());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPoster("http://image.tmdb.org/t/p/w500"+dto.getPoster());
        movie.setRecommendedContent(getLatestRecom(userId));
        return movieRepository.save(movie);
    }
    /**
     * Redis 영화 저장
     */
    public void saveMovieToRedis(List<MovieDto> movieList){
        if(movieList == null || movieList.isEmpty()){
            throw new IllegalArgumentException("movie list is empty(Redis)");
        }
        MovieDto dto = movieList.get(0);
        String searchPattern = "movie:*"+dto.getTitle();
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


