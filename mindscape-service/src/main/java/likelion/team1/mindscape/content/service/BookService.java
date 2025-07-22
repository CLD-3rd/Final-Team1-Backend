package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.BookResponse;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.RecomContent;
import likelion.team1.mindscape.content.repository.BookRepository;
import likelion.team1.mindscape.content.repository.RecomConentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    @Value("${service.api.kakaobooks}")
    private String kakaoApi;

    private final BookRepository bookRepository;
    private final RecomConentRepository recomContentRepository;

    public List<BookResponse> getBooksDetails(List<String> titles) throws IOException {
        List<BookResponse> books = new ArrayList<>();
        for (String title : titles) {
            books.add(getBookDetail(title));
        }
        return books;
    }

    public BookResponse getBookDetail(String title) throws IOException {
        String apiURL = "https://dapi.kakao.com/v3/search/book?query=";
        // set query and request url -> create url object
        String query = URLEncoder.encode(title, "UTF-8");
        String reqUrl = apiURL + query;
        URL url = new URL(reqUrl);

        // open http connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "KakaoAK " + kakaoApi);

        // 200 = get inputstream, else = get errorstream
        int responseCode = connection.getResponseCode();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()
                )
        );

        // read response line -> parse into jsonobject
        JSONObject bookJson = new JSONObject(br.readLine());
        // extract "documents" array (incl. all books w/ same title)
        JSONArray bookInfo = bookJson.getJSONArray("documents");
        // get first book from bookInfo
        JSONObject book = bookInfo.getJSONObject(0);

        // author = jsonarray type => extract authors and make it into string type
        JSONArray authorsArray = book.getJSONArray("authors");
        String authors = String.join(", ", authorsArray.toList().stream()
                .map(Object::toString)
                .toArray(String[]::new));

        // make result into bookresponse
        BookResponse bookResponse = new BookResponse(
                (String) book.get("title"),
                authors,
                (String) book.get("contents"),
                (String) book.get("thumbnail"));

        return bookResponse;
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
}
