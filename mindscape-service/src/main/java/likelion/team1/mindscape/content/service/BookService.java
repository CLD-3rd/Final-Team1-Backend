package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final RecomContentRepository recomContentRepository;

    public List<BookResponse> getBooksDetails(List<String> titles) throws IOException {
        List<BookResponse> books = new ArrayList<>();
        for (String title : titles) {
            BookResponse detail = getBookDetail(title);
            if (detail != null) {
                books.add(detail);
            } else {
                log.warn("Skip book '{}' - no result from Kakao API", title);
            }
        }
        return books;
    }

    public BookResponse getBookDetail(String title) throws IOException {
        return fetchFromKakao(title);
    }

    @Transactional
    public List<Book> saveBook(List<BookDto> bookList, Long userId) {
        // book 조회
        if (bookList == null || bookList.isEmpty()) {
            throw new IllegalArgumentException("book list is empty");
        }
        RecomContent recom = recomContentRepository.findLatestByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("추천 결과가 없습니다."));

        List<Book> toPersist = new ArrayList<>();

        for (BookDto dto : bookList) {
            bookRepository.findByTitle(dto.getTitle())
                    .ifPresentOrElse(book -> {
                        // existed -> refresh recom info
                        book.setRecommendedContent(recom);
                        toPersist.add(book);
                    }, () -> {
                        // new book
                        Book book = new Book();
                        book.setTitle(dto.getTitle());
                        book.setAuthor(dto.getAuthor());
                        book.setDescription(dto.getDescription());
                        book.setImage(dto.getImage());
                        book.setRecommendedContent(recom);
                        toPersist.add(book);
                    });
        }
        return bookRepository.saveAll(toPersist);
    }

    public void saveBookToRedis(List<BookDto> bookList) {
        if (bookList == null || bookList.isEmpty()) {
            throw new IllegalArgumentException("book list is empty(Redis)");
        }
        for (BookDto dto : bookList) {
            String searchPattern = REDIS_BOOK_KEY_PREFIX + "*" + dto.getTitle();
            Set<String> keys = redisTemplate.keys(searchPattern);
            if (keys != null && !keys.isEmpty()) {
                log.info(dto.getTitle() + ": redis에 이미 존재");
                continue;
            }
            // redis 저장
            Long id = redisService.BookToRedis(dto);
            log.info(dto.getTitle() + ": redis에 저장 완료 (id=" + id + ")");
        }
    }

    public List<Book> getBooksWithTestId(Long testId) throws IOException {
        // 1. get recomm ID
        Long recomId = testId; // testId = recomId

        List<Book> bookList = bookRepository.findAllByRecommendedContent_RecomId(recomId);
        List<Book> toSave = new ArrayList<>();
        List<String> bookTitles = bookList.stream().map(Book::getTitle).collect(Collectors.toList());


        for (Book book : bookList) {
            BookResponse info = resolveBookInfo(book.getTitle(), bookTitles);
            if (info == null) {
                log.warn("'{}' - Kakao api returned nothing and no alternative found in redis", book.getTitle());
                continue;
            }
            applyBookInfo(book, info);
            toSave.add(book);
        }
        return bookRepository.saveAll(toSave);
    }
    private BookResponse resolveBookInfo(String title, List<String> bookTitles) throws IOException {
        String key = REDIS_BOOK_KEY_PREFIX + title;

        // check redis
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(key);

        if (cached != null && !cached.isEmpty()) {
            return fromRedis(cached);
        }

        // get from kakao api
        BookResponse info = fetchFromKakao(title);

        // if no result from kakao api?
        if (info == null) {
            // get alternative from redis
            info = redisService.getAlternativeBook(bookTitles);
            if (info == null) {
                return null;
            }
        } else {
            // save to redis
            redisService.BookToRedis(new BookDto(info.getTitle(), info.getAuthor(), info.getDescription(), info.getImage()));
        }
        bookTitles.add(info.getTitle());
        return info;
    }

    private BookResponse fetchFromKakao(String title) throws IOException {
        log.info("KAKAO API USED for title='{}'", title);
        String query = URLEncoder.encode(title, "UTF-8");
        URL url = new URL(KAKAO_BOOK_API_BASE + query);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "KakaoAK " + kakaoApi);

        int responseCode = connection.getResponseCode();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()
        ))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        JSONObject bookJson = new JSONObject(sb.toString());
        JSONArray bookInfo = bookJson.optJSONArray("documents");
        if (bookInfo == null || bookInfo.length() == 0) {
            log.warn("Kakao API returned no results for title='{}'", title);
            return null;
        }

        JSONObject book = bookInfo.getJSONObject(0);
        String authors = joinAuthors(book.optJSONArray("authors"));

        return new BookResponse(
                book.optString("title", title),
                authors,
                book.optString("contents", ""),
                book.optString("thumbnail", "")
        );
    }

    private String joinAuthors(JSONArray authorsArray) {
        if (authorsArray == null || authorsArray.length() == 0) {
            return "";
        }
        return String.join(", ", authorsArray.toList().stream()
                .map(Object::toString)
                .toArray(String[]::new));
    }

    private BookResponse fromRedis(Map<Object, Object> cached) {
        return new BookResponse(
                (String) cached.getOrDefault("title", ""),
                (String) cached.getOrDefault("author", ""),
                (String) cached.getOrDefault("description", ""),
                (String) cached.getOrDefault("image", "")
        );
    }

    private void applyBookInfo(Book book, BookResponse info) {
        book.setTitle(info.getTitle());
        book.setAuthor(info.getAuthor());
        book.setDescription(info.getDescription());
        book.setImage(info.getImage());
    }
}
