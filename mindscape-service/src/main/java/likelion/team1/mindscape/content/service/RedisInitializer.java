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

    @Value("${app.redis.auto-init.force-reinit:false}")
    private boolean forceReinit;

    @Value("${app.redis.auto-init.skip-if-exists:true}")
    private boolean skipIfExists;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Redis 자동 초기화 시작... (force-reinit: {}, skip-if-exists: {})",
                forceReinit, skipIfExists);

        if (skipIfExists && !forceReinit && isRedisDataExists()) {
            log.info("Redis에 이미 데이터가 존재하고 skip-if-exists가 true이므로 초기화를 건너뜁니다.");
            return;
        }

        if (forceReinit) {
            log.info("강제 재초기화 모드로 기존 더미 데이터를 삭제합니다.");
            clearExistingDummyData();
        }

        try {
            initializeRedisData();
            log.info("Redis 자동 초기화 완료!");
        } catch (Exception e) {
            log.error("Redis 자동 초기화 중 오류 발생: {}", e.getMessage(), e);
            // 개발 환경에서는 예외를 던져서 문제를 빨리 파악하도록 하고,
            // 운영 환경에서는 계속 진행하도록 할 수 있습니다.
            // throw e; // 초기화 실패 시 애플리케이션 중단하려면 주석 해제
        }
    }

    /**
     * 수동 호출용 메서드 (Controller에서 사용)
     */
    public void manualInit() throws Exception {
        log.info("Redis 수동 초기화 시작...");
        initializeRedisData();
        log.info("Redis 수동 초기화 완료!");
    }

    /**
     * Redis에 데이터가 이미 존재하는지 확인
     */
    private boolean isRedisDataExists() {
        Set<String> movieKeys = redisTemplate.keys("movie:*");
        Set<String> bookKeys = redisTemplate.keys("book:*");
        Set<String> musicKeys = redisTemplate.keys("music:*");

        boolean hasMovies = movieKeys != null && !movieKeys.isEmpty();
        boolean hasBooks = bookKeys != null && !bookKeys.isEmpty();
        boolean hasMusic = musicKeys != null && !musicKeys.isEmpty();

        log.info("Redis 데이터 존재 여부 - Movies: {}, Books: {}, Music: {}",
                hasMovies, hasBooks, hasMusic);

        // 모든 타입의 데이터가 하나라도 있으면 데이터가 존재한다고 판단
        return hasMovies || hasBooks || hasMusic;
    }

    /**
     * 기존 더미 데이터 삭제 (강제 재초기화용)
     */
    private void clearExistingDummyData() {
        List<String> dummyTitles = List.of("쇼생크 탈출", "인셉션", "매트릭스",
                "앵무새 죽이기", "1984", "연금술사",
                "Bohemian Rhapsody", "Let It Be", "Like A Rolling Stone");

        for (String title : dummyTitles) {
            try {
                Set<String> keys = redisTemplate.keys("*" + title + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.info("기존 더미 데이터 삭제: {}", title);
                }
            } catch (Exception e) {
                log.warn("더미 데이터 삭제 실패: {} - {}", title, e.getMessage());
            }
        }
    }

    /**
     * Redis에 초기 더미 데이터 저장
     */
    private void initializeRedisData() throws IOException {
        log.info("Redis 더미 데이터 생성 중...");

        // 더미 데이터 정의
        List<String> moviesList = List.of("쇼생크 탈출", "인셉션", "매트릭스");
        List<String> booksList = List.of("앵무새 죽이기", "1984", "연금술사");
        List<String> musicList = List.of(
                "Queen - Bohemian Rhapsody",
                "The Beatles - Let It Be",
                "Bob Dylan - Like A Rolling Stone"
        );

        List<MovieDto> movieDtos = new ArrayList<>();
        List<MusicDto> musicDtos = new ArrayList<>();
        List<BookDto> bookDtos = new ArrayList<>();

        // 외부 API에서 상세 정보 가져오기
        try {
            for (int i = 0; i < moviesList.size(); i++) {
                // 영화 정보 가져오기
                List<MovieDto> movieInfo = movieService.getMovieInfo(moviesList.get(i));
                if (!movieInfo.isEmpty()) {
                    movieDtos.add(movieInfo.get(0));
                    log.info("영화 정보 가져옴: {}", moviesList.get(i));
                } else {
                    log.warn("영화 정보를 찾을 수 없음: {}", moviesList.get(i));
                }

                // 책 정보 가져오기
                try {
                    BookDto bookDto = BookDto.from(bookService.getBookDetail(booksList.get(i)));
                    bookDtos.add(bookDto);
                    log.info("책 정보 가져옴: {}", booksList.get(i));
                } catch (Exception e) {
                    log.warn("책 정보를 가져오는데 실패: {} - {}", booksList.get(i), e.getMessage());
                }

                // 음악 정보 가져오기
                String[] tmp = musicList.get(i).split("[–-]", 2);
                if (tmp.length == 2) {
                    try {
                        MusicDto musicDto = MusicDto.from(
                                musicService.getMusicDetail(tmp[0].trim(), tmp[1].trim())
                        );
                        musicDtos.add(musicDto);
                        log.info("음악 정보 가져옴: {}", musicList.get(i));
                    } catch (Exception e) {
                        log.warn("음악 정보를 가져오는데 실패: {} - {}", musicList.get(i), e.getMessage());
                    }
                } else {
                    log.warn("잘못된 음악 형식: {}", musicList.get(i));
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
}