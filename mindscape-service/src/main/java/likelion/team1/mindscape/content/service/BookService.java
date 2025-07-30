package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.TestInfoResponse;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.BookResponse;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.repository.BookRepository;
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
                .filter(dto -> !hasRedisHash(makeRedisKey(dto.getTitle())))
                .forEach(dto -> {
                    log.info("[REDIS@BookService.saveBookToRedis] Save movie '{}'", dto.getTitle());
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

        log.info("[SQL@BookService.getBooksWithTestId] Find books by recomId={}", recomId);
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
                log.warn("[featch@BookService.getBooksWithTestId] Returned nothing and no alternative found in Redis: {}", book.getTitle());
                continue;
            }
            applyBookInfo(book, info);
            toSave.add(book);
        }
        log.info("[SQL@BookService.getBooksWithTestId] Save {} books", toSave.size());
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
            BookResponse cachedInfo = cached.get();
            if (isComplete(cachedInfo)) {
                log.info("[REDIS@BookService.resolveBookInfo] Found book '{}'", title);
                return cachedInfo;
            } else {
                log.warn("[REDIS@BookService.resolveBookInfo] Incomplete data '{}'. Refresh via API", title);
            }
        }

        BookResponse info = fetchFromKakao(title);

        // 3. Kakao 실패 또는 불완전 데이터 시 Redis에서 대체 찾기
        if (!isComplete(info)) {
            if (info == null) {
                log.warn("[KAKAO API@BookService.resolveBookInfo] Failed for '{}'. Get alternative from Redis", title);
            } else {
                log.warn("[KAKAO API@BookService.resolveBookInfo] Incomplete BookResponse '{}'. Get alternative from Redis", title);
            }
            info = redisService.getAlternativeBook(new ArrayList<>(usedTitles));
            if (!isComplete(info)) {
                log.error("[REDIS@BookService.resolveBookInfo] No alternative found in Redis for '{}'", title);
                return null;
            }
            log.info("[REDIS@BookService.resolveBookInfo] Use alternative '{}'", info.getTitle());
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
        log.info("[KAKAO API@BookService.fetchFromKakao] Kakao API used for title='{}'", title);

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
            log.warn("[KAKAO API@BookService.fetchFromKakao] Kakao API returned no results for title='{}'", title);
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
        try {
            log.info("[REDIS@BookService.hasRedisHash] Check key type {}", key);
            String type = redisTemplate.type(key).code();
            if (!"hash".equals(type) && !"none".equals(type)) {
                log.warn("[REDIS@BookService.hasRedisHash] Key {} is not hash type: {}", key, type);
                return false;
            }
            if ("none".equals(type)) {
                return false;
            }
            log.info("[REDIS@BookService.hasRedisHash] Get hash size {}", key);
            Long size = redisTemplate.opsForHash().size(key);
            return size != null && size > 0;
        } catch (Exception e) {
            log.error("[REDIS@BookService.hasRedisHash] Error checking key {}: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Helper
     * Redis 키 생성
     *
     * @param title
     * @return
     */
    private String makeRedisKey(String title) {
        return REDIS_BOOK_KEY_PREFIX + title;
    }

    /**
     * Helper
     * Redis에서 title로 정보 가져오기
     *
     * @param title
     * @return
     */
    private Optional<BookResponse> getFromRedis(String title) {
        String key = makeRedisKey(title);
        try {
            log.info("[REDIS@BookService.getFromRedis] Check key type {}", key);
            // 키 타입 확인
            String type = redisTemplate.type(key).code();
            if (!"hash".equals(type)) {
                log.debug("[REDIS@BookService.getFromRedis] Key {} is not hash type: {}", key, type);
                return Optional.empty();
            }
            log.info("[REDIS@BookService.getFromRedis] Load hash {}", key);
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            if (map == null || map.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(BookResponse.fromRedis(map));
        } catch (Exception e) {
            log.error("[REDIS@BookService.getFromRedis] Error getting book with key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Helper
     * Redis에 새로운 정보 저장
     *
     * @param info
     */
    private void cacheToRedis(BookResponse info) {
        log.info("[REDIS@BookService.cacheToRedis] Save book to Redis title='{}'", info.getTitle());
        redisService.BookToRedis(new BookDto(info.getTitle(), info.getAuthor(), info.getDescription(), info.getImage()));
    }

    /**
     * Helper
     * Kakao/Redis에서 얻은 BookResponse가 모든 필드를 채웠는지 검증
     *
     * @param info
     * @return
     */
    private boolean isComplete(BookResponse info) {
        return info != null
                && notNullOrBlank(info.getTitle())
                && notNullOrBlank(info.getAuthor())
                && notNullOrBlank(info.getDescription())
                && notNullOrBlank(info.getImage());
    }
    private boolean notNullOrBlank(String s) {
        return s != null && !s.trim().isEmpty();
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