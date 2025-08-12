package likelion.team1.mindscape.content.service;

import likelion.team1.mindscape.content.dto.ContentCountDto;
import likelion.team1.mindscape.content.dto.response.HistoryResponse;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.entity.Music;
import likelion.team1.mindscape.content.repository.BookRepository;
import likelion.team1.mindscape.content.repository.MovieRepository;
import likelion.team1.mindscape.content.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final BookRepository bookRepository;
    private final MovieRepository movieRepository;
    private final MusicRepository musicRepository;

    /**
     * UserID 로 test 테이블에서 해당 사용자의 testId 전부 가져오기
     *
     * @param userId
     * @return
     */
    public List<Long> getTestIdByUserId(Long userId) {
        //TODO: test테이블에서 userId로 testId 가져오기
        List<Long> testIdList = new ArrayList<>();

        return testIdList;
    }

    /**
     * TestId로 BookDto 가져오기
     *
     * @param testId
     * @return
     */
    public List<BookDto> getBookDtoByTestId(Long testId) {
        List<BookDto> bookList = new ArrayList<>();
        List<Object> book = chkRedis("book", testId);
        List<Map<Object, Object>> bookDetails = getDetailsFromRedis("book", book);

        if (book.isEmpty() || bookDetails.isEmpty()) {
            for (Map<Object, Object> map : getFromSql("book", testId)) {
                bookList.add(toBookDto(map));
            }
        } else {
            for (Map<Object, Object> map : bookDetails) {
                bookList.add(toBookDto(map));
            }
        }
        return bookList;
    }

    /**
     * TestId로 MusicDto 가져오기
     *
     * @param testId
     * @return
     */
    public List<MusicDto> getMusicDtoByTestId(Long testId) {
        List<MusicDto> musicList = new ArrayList<>();
        List<Object> music = chkRedis("music", testId);
        List<Map<Object, Object>> musicDetails = getDetailsFromRedis("music", music);

        if (music.isEmpty() || musicDetails.isEmpty()) {
            for (Map<Object, Object> map : getFromSql("music", testId)) {
                musicList.add(toMusicDto(map));
            }
        } else {
            for (Map<Object, Object> map : musicDetails) {
                musicList.add(toMusicDto(map));
            }
        }
        return musicList;
    }

    /**
     * TestId로 MovieDto 가져오기
     *
     * @param testId
     * @return
     */
    public List<MovieDto> getMovieDtoByTestId(Long testId) {
        List<MovieDto> movieList = new ArrayList<>();
        List<Object> movie = chkRedis("movie", testId);
        List<Map<Object, Object>> movieDetails = getDetailsFromRedis("movie", movie);

        if (movie.isEmpty() || movieDetails.isEmpty()) {
            for (Map<Object, Object> map : getFromSql("movie", testId)) {
                movieList.add(toMovieDto(map));
            }
        } else {
            for (Map<Object, Object> map : movieDetails) {
                movieList.add(toMovieDto(map));
            }
        }
        return movieList;
    }

    /**
     * Test ID로 size만큼 저장 많이 되어 있는 것 가져오기
     *
     * @param ids
     * @param size
     * @return
     */
    public HistoryResponse getRankingResponse(List<Long> ids, int size) {
        List<BookDto> books = getTopBooksByIds(ids, size);
        List<MusicDto> music = getTopMusicByIds(ids, size);
        List<MovieDto> movie = getTopMoviesByIds(ids, size);

        HistoryResponse.Recommend recommend = new HistoryResponse.Recommend(books, music, movie);
        return new HistoryResponse(0L, recommend);
    }

    /**
     * Test ID로 sql에서 책 size만큼 가져오기
     *
     * @param ids
     * @param size
     * @return
     */
    private List<BookDto> getTopBooksByIds(List<Long> ids, int size) {
        List<Long> bookIds = getContentCountByType("book", ids, size)
                .stream().map(book -> book.getIds().get(0)).toList();

        List<BookDto> books = new ArrayList<>();
        for (Long id : bookIds) {
            Book tmp = bookRepository.findById(id).orElse(null);
            if (tmp != null) {
                books.add(new BookDto(tmp.getTitle(), tmp.getAuthor(), tmp.getDescription(), tmp.getImage()));
            }
        }
        return books;
    }

    /**
     * Test ID로 sql에서 음악 size만큼 가져오기
     *
     * @param ids
     * @param size
     * @return
     */
    private List<MusicDto> getTopMusicByIds(List<Long> ids, int size) {
        List<Long> musicIds = getContentCountByType("music", ids, size)
                .stream().map(music -> music.getIds().get(0)).toList();

        List<MusicDto> music = new ArrayList<>();
        for (Long id : musicIds) {
            Music tmp = musicRepository.findById(id).orElse(null);
            if (tmp != null) {
                music.add(new MusicDto(tmp.getTitle(), tmp.getArtist(), tmp.getElbum()));
            }
        }
        return music;
    }

    /**
     * Test ID로 sql에서 영화 size만큼 가져오기
     *
     * @param ids
     * @param size
     * @return
     */
    private List<MovieDto> getTopMoviesByIds(List<Long> ids, int size) {
        List<Long> movieIds = getContentCountByType("movie", ids, size)
                .stream().map(movie -> movie.getIds().get(0)).toList();

        List<MovieDto> movie = new ArrayList<>();
        for (Long id : movieIds) {
            Movie tmp = movieRepository.findById(id).orElse(null);
            if (tmp != null) {
                movie.add(new MovieDto(tmp.getTitle(), tmp.getReleaseDate(), tmp.getDescription(), tmp.getPoster()));
            }
        }
        return movie;
    }

    /**
     * Test ID로 sql에서 컨텐츠의 개수를 개수가 많은 순으로 limit만큼 가져오기
     *
     * @param contentType
     * @param recomIds
     * @param limit
     * @return
     */
    private List<ContentCountDto> getContentCountByType(String contentType, List<Long> recomIds, int limit) {
        switch (contentType.toLowerCase()) {
            case "book":
                List<Object[]> bookResults = bookRepository.getBookCountWithIds(recomIds, limit);
                return bookResults.stream()
                        .map(row -> {
                            String title = (String) row[0];
                            Long cnt = ((Number) row[1]).longValue();
                            String recomIdsStr = (String) row[2];
                            List<Long> recomIdsList = Arrays.stream(recomIdsStr.split(","))
                                    .map(Long::parseLong)
                                    .collect(Collectors.toList());
                            return new ContentCountDto(title, cnt, recomIdsList);
                        })
                        .collect(Collectors.toList());

            case "movie":
                List<Object[]> movieResults = movieRepository.getMovieCountWithIds(recomIds, limit);
                return movieResults.stream()
                        .map(row -> {
                            String title = (String) row[0];
                            Long cnt = ((Number) row[1]).longValue();
                            String recomIdsStr = (String) row[2];
                            List<Long> recomIdsList = Arrays.stream(recomIdsStr.split(","))
                                    .map(Long::parseLong)
                                    .collect(Collectors.toList());
                            return new ContentCountDto(title, cnt, recomIdsList);
                        })
                        .collect(Collectors.toList());

            case "music":
                List<Object[]> musicResults = musicRepository.getMusicCountWithIds(recomIds, limit);
                return musicResults.stream()
                        .map(row -> {
                            String title = (String) row[0];
                            Long cnt = ((Number) row[1]).longValue();
                            String recomIdsStr = (String) row[2];
                            List<Long> recomIdsList = Arrays.stream(recomIdsStr.split(","))
                                    .map(Long::parseLong)
                                    .collect(Collectors.toList());
                            return new ContentCountDto(title, cnt, recomIdsList);
                        })
                        .collect(Collectors.toList());

            default:
                throw new IllegalArgumentException("Unknown content type: " + contentType);
        }
    }

    /**
     * Redis에 저장되어 있는지 확인
     *
     * @param type
     * @param testId
     * @return
     */
    private List<Object> chkRedis(String type, Long testId) {
        String pattern = "user:*:test:" + testId + ":" + type;
        log.info("Redis checking for {}, testId: {}", type, testId);
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object> results = new ArrayList<>();
        for (String key : keys) {
            List<Object> list = redisTemplate.opsForList().range(key, 0, -1);
            if (list != null) {
                results.addAll(list);
            }
        }
        return results;
    }

    /**
     * Title로 Redis에서 정보 가져오기
     *
     * @param type
     * @param list
     * @return
     */
    private List<Map<Object, Object>> getDetailsFromRedis(String type, List<Object> list) {
        List<Map<Object, Object>> res = new ArrayList<>();
        for (Object o : list) {
            if (res.size() == 3) return res;
            String key = type + ":" + o;
            log.info("Redis in use for {}", key);
            res.add(redisTemplate.opsForHash().entries(key));
        }
        return res;
    }

    /**
     * Sql에 저장되어 있는 정보 가져오기
     *
     * @param type
     * @param testId
     * @return
     */
    private List<Map<Object, Object>> getFromSql(String type, Long testId) {
        Long recomId = testId;

        List<Map<Object, Object>> res = new ArrayList<>();

        if (type == "book") {
            log.info("SQL in use for {}, recomId: {}", type, recomId);
            List<Book> books = bookRepository.findTop3AllByRecommendedContent_RecomId(recomId);
            for (Book book : books) {
                Map<Object, Object> tmp = new HashMap<>();
                tmp.put("title", book.getTitle());
                tmp.put("author", book.getAuthor());
                tmp.put("description", book.getDescription());
                tmp.put("image", book.getImage());
                res.add(tmp);
            }
            return res;
        }

        if (type == "music") {
            log.info("SQL in use for {}, recomId: {}", type, recomId);
            List<Music> music = musicRepository.findTop3AllByRecommendedContent_RecomId(recomId);
            for (Music m : music) {
                Map<Object, Object> tmp = new HashMap<>();
                tmp.put("title", m.getTitle());
                tmp.put("artist", m.getArtist());
                tmp.put("album", m.getElbum());
                res.add(tmp);
            }
            return res;
        }

        if (type == "movie") {
            log.info("SQL in use for {}, recomId: {}", type, recomId);
            List<Movie> movies = movieRepository.findTop3AllByRecommendedContent_RecomId(recomId);
            for (Movie movie : movies) {
                Map<Object, Object> tmp = new HashMap<>();
                tmp.put("title", movie.getTitle());
                tmp.put("release_date", movie.getReleaseDate());
                tmp.put("description", movie.getDescription());
                tmp.put("poster", movie.getPoster());
                res.add(tmp);
            }
            return res;
        }
        return null;
    }

    /**
     * 가져온 정보를 BookDto로 변환
     *
     * @param bookMap
     * @return
     */
    private BookDto toBookDto(Map<Object, Object> bookMap) {
        BookDto res = new BookDto(
                (String) bookMap.get("title"),
                (String) bookMap.get("author"),
                (String) bookMap.get("description"),
                (String) bookMap.get("image")
        );
        return res;
    }

    /**
     * 가져온 정보를 MusicDto로 변환
     *
     * @param musicMap
     * @return
     */
    private MusicDto toMusicDto(Map<Object, Object> musicMap) {
        MusicDto res = new MusicDto(
                (String) musicMap.get("title"),
                (String) musicMap.get("artist"),
                (String) musicMap.get("album")
        );
        return res;
    }

    /**
     * 가져온 정보를 MovieDto로 변환
     *
     * @param movieMap
     * @return
     */
    private MovieDto toMovieDto(Map<Object, Object> movieMap) {
        String title = (String) movieMap.get("title");
        Object releaseDateObj = movieMap.get("release_date");
        String description = (String) movieMap.get("description");
        String poster = (String) movieMap.get("poster");

        Date releaseDate = null;
        if (releaseDateObj instanceof String) {
            try {
                releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse((String) releaseDateObj);
            } catch (ParseException e) {
                log.warn("Invalid date format: {}", releaseDateObj);
            }
        } else if (releaseDateObj instanceof java.sql.Timestamp) {
            releaseDate = new Date(((java.sql.Timestamp) releaseDateObj).getTime());
        } else if (releaseDateObj instanceof Date) {
            releaseDate = (Date) releaseDateObj;
        }

        return new MovieDto(title, releaseDate, description, poster);
    }
}
