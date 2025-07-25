package likelion.team1.mindscape.content.service;

import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.redis.auto-init.enabled",
        havingValue = "true",
        matchIfMissing = true  // 설정이 없으면 기본적으로 활성화
)
public class RedisInitializer implements ApplicationRunner {

    private final MovieService movieService;
    private final BookService bookService;
    private final MusicService musicService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final List<String> MOVIESLIST = List.of("쇼생크 탈출", "인셉션", "매트릭스");
    private final List<String> BOOKSLIST = List.of("앵무새 죽이기", "1984", "연금술사");
    private final List<String> MUSICLIST = List.of(
            "Queen - Bohemian Rhapsody",
            "The Beatles - Let It Be",
            "Bob Dylan - Like A Rolling Stone"
    );

    private final int NUMBER_OF_CONTENTS = 3;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Redis 자동 초기화 시작");

        if (isRedisDataExists()) {
            log.info("Redis에 이미 데이터가 존재하므로 초기화를 건너뜁니다.");
            return;
        }

        try {
            initializeRedisData();
            log.info("Redis 자동 초기화 완료!");
        } catch (Exception e) {
            log.error("Redis 자동 초기화 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private boolean isRedisDataExists() {
        Set<String> movieKeys = redisTemplate.keys("movie:*");
        Set<String> bookKeys = redisTemplate.keys("book:*");
        Set<String> musicKeys = redisTemplate.keys("music:*");

        long movieCount = countContentItems(movieKeys, "movie:id");
        long bookCount = countContentItems(bookKeys, "book:id");
        long musicCount = countContentItems(musicKeys, "music:id");

        boolean hasMovies = movieCount >= NUMBER_OF_CONTENTS;
        boolean hasBooks = bookCount >= NUMBER_OF_CONTENTS;
        boolean hasMusic = musicCount >= NUMBER_OF_CONTENTS;

        log.info("Redis 데이터 존재 여부 - Movies: {} ({}개), Books: {} ({}개), Music: {} ({}개)",
                hasMovies, movieCount, hasBooks, bookCount, hasMusic, musicCount);

        return hasMovies && hasBooks && hasMusic;
    }


    /**
     * Redis에 초기 더미 데이터 저장
     */
    private void initializeRedisData() throws IOException {
        log.info("Redis 더미 데이터 생성 중...");

        List<MovieDto> movieDtos = new ArrayList<>();
        List<MusicDto> musicDtos = new ArrayList<>();
        List<BookDto> bookDtos = new ArrayList<>();

        // 외부 API에서 상세 정보 가져오기
        try {
            for (int i = 0; i < MOVIESLIST.size(); i++) {
                // 영화 정보 가져오기
                List<MovieDto> movieInfo = movieService.getMovieInfo(MOVIESLIST.get(i));
                if (!movieInfo.isEmpty()) {
                    movieDtos.add(movieInfo.get(0));
                    log.info("영화 정보 가져옴: {}", MOVIESLIST.get(i));
                } else {
                    log.warn("영화 정보를 찾을 수 없음: {}", MOVIESLIST.get(i));
                }

                // 책 정보 가져오기
                try {
                    BookDto bookDto = BookDto.from(bookService.getBookDetail(BOOKSLIST.get(i)));
                    bookDtos.add(bookDto);
                    log.info("책 정보 가져옴: {}", BOOKSLIST.get(i));
                } catch (Exception e) {
                    log.warn("책 정보를 가져오는데 실패: {} - {}", BOOKSLIST.get(i), e.getMessage());
                }

                // 음악 정보 가져오기
                String[] tmp = MUSICLIST.get(i).split("[–-]", 2);
                if (tmp.length == 2) {
                    try {
                        MusicDto musicDto = MusicDto.from(
                                musicService.getMusicDetail(tmp[0].trim(), tmp[1].trim())
                        );
                        musicDtos.add(musicDto);
                        log.info("음악 정보 가져옴: {}", MUSICLIST.get(i));
                    } catch (Exception e) {
                        log.warn("음악 정보를 가져오는데 실패: {} - {}", MUSICLIST.get(i), e.getMessage());
                    }
                } else {
                    log.warn("잘못된 음악 형식: {}", MUSICLIST.get(i));
                }
            }
        } catch (Exception e) {
            log.error("외부 API 호출 중 오류 발생", e);
            throw e;
        }

        // Redis에 데이터 저장
        saveDataToRedis(movieDtos, musicDtos, bookDtos);
    }

    /**
     * 가져온 데이터를 Redis에 저장
     */
    private void saveDataToRedis(List<MovieDto> movieDtos, List<MusicDto> musicDtos, List<BookDto> bookDtos) {
        // 영화 데이터 저장
        for (MovieDto movieDto : movieDtos) {
            try {
                movieService.saveMovieToRedis(List.of(movieDto));
                log.info("Redis에 영화 저장: {}", movieDto.getTitle());
            } catch (Exception e) {
                log.error("영화 Redis 저장 실패: {} - {}", movieDto.getTitle(), e.getMessage());
            }
        }

        // 음악 데이터 저장
        if (!musicDtos.isEmpty()) {
            try {
                musicService.saveMusicToRedis(musicDtos);
                log.info("Redis에 음악 {} 곡 저장 완료", musicDtos.size());
            } catch (Exception e) {
                log.error("음악 Redis 저장 실패: {}", e.getMessage());
            }
        }

        // 책 데이터 저장
        if (!bookDtos.isEmpty()) {
            try {
                bookService.saveBookToRedis(bookDtos);
                log.info("Redis에 책 {} 권 저장 완료", bookDtos.size());
            } catch (Exception e) {
                log.error("책 Redis 저장 실패: {}", e.getMessage());
            }
        }

        log.info("Redis 데이터 저장 완료 - 영화: {}, 음악: {}, 책: {}",
                movieDtos.size(), musicDtos.size(), bookDtos.size());
    }

    private long countContentItems(Set<String> keys, String idKey) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        String prefix = idKey.substring(0, idKey.indexOf(':') + 1);

        long count = 0;
        for (String key : keys) {
            if (key.equals(idKey)) {
                continue;
            }
            if (key.startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }
}