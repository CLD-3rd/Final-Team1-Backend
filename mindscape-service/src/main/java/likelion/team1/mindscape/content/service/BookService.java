package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.TestInfoResponse;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.BookResponse;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.RecomContent;
import likelion.team1.mindscape.content.repository.BookRepository;
import likelion.team1.mindscape.content.repository.RecomContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    private static final String KAKAO_BOOK_API_BASE = "https://dapi.kakao.com/v3/search/book?query=";
    private static final String REDIS_BOOK_KEY_PREFIX = "book:";
    @Value("${service.api.kakaobooks}")
    private String kakaoApi;

    private final BookRepository bookRepository;
    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ContentService contentService;
    private final TestServiceClient testServiceClient;

    /**
     * 단건 상세 조회 (외부 API 직접 호출)
     *
     * @param title
     * @return
     * @throws IOException
     */
    public BookResponse getBookDetail(String title) throws IOException {
        return fetchFromKakao(title);
    }

    /**
     * 입력된 BookDto 리스트를 Redis에 저장 (이미 있으면 스킵)
     *
     * @param bookList
     */
    public void saveBookToRedis(List<BookDto> bookList) {
        if (bookList == null || bookList.isEmpty()) {
            throw new IllegalArgumentException("book list is empty(Redis)");
        }

        bookList.stream()
                .filter(dto -> !hasRedisHash(normalizeKey(dto.getTitle())))
                .forEach(dto -> {
                    Long id = redisService.BookToRedis(dto);
                    log.info("'{}' : redis에 저장 완료 (id={})", dto.getTitle(), id);
                });
    }

    /**
     * testId(recomId)로 저장된 책 목록을 가져온 뒤,
     * 각각의 책에 대해 Redis -> Kakao API -> Redis 대체 순으로 정보를 보강
     *
     * @param testId
     * @return
     * @throws IOException
     */
    @Transactional
    public List<Book> getBooksWithTestId(Long testId) throws IOException {
        TestInfoResponse testInfo = testServiceClient.getTestInfo(testId);
        Long userId = testInfo.getUserId();
        Long recomId = testId; // testId = recomId

        List<Book> books = bookRepository.findAllByRecommendedContent_RecomId(recomId);
        if (books.isEmpty()) {
            return books;
        }

        // 중복 방지를 위한 이미 사용된 타이틀 모음
        Set<String> usedTitles = books.stream()
                .map(Book::getTitle)
                .collect(Collectors.toCollection(HashSet::new));

        List<Book> toSave = new ArrayList<>();
        for (Book book : books) {
            BookResponse info = resolveBookInfo(book.getTitle(), usedTitles);
            if (info == null) {
                log.warn("'{}' - Kakao api returned nothing and no alternative found in redis", book.getTitle());
                continue;
            }
            applyBookInfo(book, info);
            toSave.add(book);
        }

        List<Book> saved = bookRepository.saveAll(toSave);
        List<String> finalTitles = saved.stream().map(Book::getTitle).toList();
        contentService.saveRecomContent(userId, testId, "book", finalTitles);

        return saved;
    }

    /**
     * Helper
     * Redis -> Kakao API -> Redis 대체 순으로 BookResponse를 획득
     *
     * @param title
     * @param usedTitles
     * @return
     * @throws IOException
     */
    private BookResponse resolveBookInfo(String title, Set<String> usedTitles) throws IOException {
        // 1. Redis 조회
        Optional<BookResponse> cached = getFromRedis(title);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. Kakao API 조회
        BookResponse info = fetchFromKakao(title);

        // 3. Kakao 실패 시 Redis에서 대체 찾기
        if (info == null) {
            info = redisService.getAlternativeBook(new ArrayList<>(usedTitles));
            if (info == null) {
                return null;
            }
        } else {
            cacheToRedis(info);
        }

        usedTitles.add(info.getTitle());
        return info;
    }

    /**
     * Helper
     * Kakao API 호출
     *
     * @param title
     * @return
     * @throws IOException
     */
    private BookResponse fetchFromKakao(String title) throws IOException {
        log.info("KAKAO API USED for title='{}'", title);

        String query = URLEncoder.encode(title, StandardCharsets.UTF_8);
        URL url = new URL(KAKAO_BOOK_API_BASE + query);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "KakaoAK " + kakaoApi);

        int responseCode = connection.getResponseCode();
        String body = readBody(connection, responseCode == 200);
        connection.disconnect();

        JSONObject bookJson = new JSONObject(body);
        JSONArray docs = bookJson.optJSONArray("documents");
        if (docs == null || docs.length() == 0) {
            log.warn("Kakao API returned no results for title='{}'", title);
            return null;
        }

        JSONObject first = docs.getJSONObject(0);
        return parseBookResponse(first, title);
    }

    /**
     * Helper
     * JSONArrary로 이루어진 Authors를 String으로 변환
     *
     * @param authorsArray
     * @return
     */
    private String joinAuthors(JSONArray authorsArray) {
        if (authorsArray == null || authorsArray.length() == 0) {
            return "";
        }
        return authorsArray.toList()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Helper
     * 기존 Book 객체를 새로운 BookResponse정보로 교체
     *
     * @param book
     * @param info
     */
    private void applyBookInfo(Book book, BookResponse info) {
        book.setTitle(info.getTitle());
        book.setAuthor(info.getAuthor());
        book.setDescription(info.getDescription());
        book.setImage(info.getImage());
    }

    /**
     * Helper
     * Redis에 key 있는지 조회, 있으면 True
     *
     * @param key
     * @return
     */
    private boolean hasRedisHash(String key) {
        Long size = redisTemplate.opsForHash().size(key);
        return size != null && size > 0;
    }

    /**
     * Helper
     * Title 한국어 대응
     *
     * @param rawTitle
     * @return
     */
    private String normalizeKey(String rawTitle) {
        return REDIS_BOOK_KEY_PREFIX + Base64.getUrlEncoder()
                .encodeToString(rawTitle.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Helper
     * Redis에서 title로 정보 가져오기
     *
     * @param title
     * @return
     */
    private Optional<BookResponse> getFromRedis(String title) {
        String key = normalizeKey(title);
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map == null || map.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(BookResponse.fromRedis(map));
    }

    /**
     * Helper
     * Redis에 새로운 정보 저장
     *
     * @param info
     */
    private void cacheToRedis(BookResponse info) {
        redisService.BookToRedis(new BookDto(info.getTitle(), info.getAuthor(), info.getDescription(), info.getImage()));
    }

    /**
     * HttpURLConnection 응답 본문 읽기
     *
     * @param connection
     * @param successStream
     * @return
     * @throws IOException
     */
    private String readBody(HttpURLConnection connection, boolean successStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                successStream ? connection.getInputStream() : connection.getErrorStream(),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Kakao 응답 JSON을 BookResponse로 변환
     *
     * @param book
     * @param fallbackTitle
     * @return
     */
    private BookResponse parseBookResponse(JSONObject book, String fallbackTitle) {
        String authors = joinAuthors(book.optJSONArray("authors"));
        return new BookResponse(
                book.optString("title", fallbackTitle),
                authors,
                book.optString("contents", ""),
                book.optString("thumbnail", "")
        );
    }
}
