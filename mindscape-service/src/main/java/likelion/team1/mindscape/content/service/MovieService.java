package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.TestInfoResponse;
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

import javax.swing.text.html.Option;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

import static likelion.team1.mindscape.content.dto.response.content.MovieDto.fromRedisHash;

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
    private final ContentService contentService;
    private final TestServiceClient testServiceClient;


    public List<Movie> updateMovieFromTitle(Long testId) {

        TestInfoResponse testInfo = testServiceClient.getTestInfo(testId);
        Long userId = testInfo.getUserId();
        List<Movie> targetMovies = movieRepository.findTop3AllByRecommendedContent_RecomId(testId);
        List<Movie> updatedList = new ArrayList<>();
        boolean isFirstRequest = true;

        // 1. movie 검색
        for (Movie movie : targetMovies) {
            if (hasCompleteInfo(movie)) { // 영화 title에 따른 내용 넣기
                updateRecomOnly(movie, testId);
                updatedList.add(movieRepository.save(movie));
            } else {
                List<MovieDto> movieInfo = getMovieInfo(movie.getTitle());
                MovieDto dto = getMovieDtoFallback(movieInfo, movie, isFirstRequest);
                if (movieInfo.isEmpty()) {
                    isFirstRequest = false;  // fallback 시도 후에 false 처리
                }
                if (dto == null) {
                    System.out.println("TMDB & Redis 모두 실패: " + movie.getTitle());
                    continue;
                }
                Movie updated = createNewMovie(movie, dto, testId);
                updatedList.add(updated);
                if (!movieInfo.isEmpty()) {
                    saveMovieToRedis(movieInfo);
                }
            }
        }
        List<String> titles = updatedList.stream()
                .map(Movie::getTitle)
                .collect(Collectors.toList());
        contentService.saveRecomContent(userId, testId, "movie", titles);
        return updatedList;
    }

    private MovieDto getMovieDtoFallback(List<MovieDto> movieInfo, Movie movie, boolean isFirstRequest) {
        if (movieInfo.isEmpty()) {
            if (isFirstRequest) {
                Optional<MovieDto> fallback = getRandomMovie(Collections.singletonList(new MovieDto(movie.getTitle())));
                fallback.ifPresent(dto ->
                        System.out.println("Redis 대체 영화 사용: " + dto.getTitle())
                );
                return fallback.orElse(null);
            } else { return null; }
        } else {
            saveMovieToRedis(movieInfo);
            return movieInfo.get(0);
        }
    }



    /**
     * tmdb api에 없는 영화: redis에서 rndm하게 아무 영화 넣기
     * @param movieList
     * @return
     */
    //TODO: Redis에서 가져올 때 추천받은 title과 중복되는지 확인하는 작업 필요
    public Optional<MovieDto> fillMovieInfo(List<Movie> movieList){
        for(Movie movie: movieList){
            if(!hasCompleteInfo(movie)){
                MovieDto tempDto = new MovieDto(movie.getTitle());
                Optional<MovieDto> fallback = getRandomMovie(Collections.singletonList(tempDto));
                return fallback;
            }
        }
        return Optional.empty();
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
    private void updateRecomOnly(Movie movie, Long testId) {
        RecomContent recom = getLatestRecom(testId);
        movie.setRecommendedContent(recom);
    }
    private RecomContent getLatestRecom(Long testId) {
        return recomContentRepository
                .findByTestIdNative(testId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("추천 결과가 없습니다."));
    }

    private Movie createNewMovie(Movie movie, MovieDto dto, Long userId){
        movie.setTitle(dto.getTitle());
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
    public Optional<MovieDto> getRandomMovie (List<MovieDto> movieInfo){
        // 기존 영화 제목
        Set<String> existingTitles = movieInfo.stream()
                .map(MovieDto::getTitle)
                .collect(Collectors.toSet());
        // redis 영화제목
        Set<String> keys = redisTemplate.keys("movie:*");
        if(keys == null || keys.isEmpty()) return Optional.empty();

        List<String> shuffledKeys = new ArrayList<>(keys);
        Collections.shuffle(shuffledKeys);

        for(String key: shuffledKeys){
            String title = key.replace("movie:","");
            if(existingTitles.contains(title)) continue;
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
            MovieDto dto = fromRedisHash(hash); // Dto로 변환
            return Optional.of(dto);
        }
        return Optional.empty();
    }
}




