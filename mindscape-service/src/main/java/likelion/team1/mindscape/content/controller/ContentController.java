package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.dto.response.content.*;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.entity.Music;
import likelion.team1.mindscape.content.enums.ContentType;
import likelion.team1.mindscape.content.service.BookService;
import likelion.team1.mindscape.content.service.MovieService;
import likelion.team1.mindscape.content.service.MusicService;
import likelion.team1.mindscape.content.service.RedisInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController {
    private final MovieService movieService;
    private final BookService bookService;
    private final MusicService musicService;
    private static final String ARTIST_TITLE_DELIMITER_REGEX = "[–-]"; // en dash or hyphen
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam ContentType content,
            @RequestParam(required = false) String query,   // MOVIE
            @RequestParam(required = false) String artist,  // MUSIC
            @RequestParam(required = false) String title    // MUSIC/BOOK
    ) throws IOException {

        Object result = switch (content) {
            case MOVIE -> {
                require(StringUtils.hasText(query), "MOVIE requires title");
                yield movieService.getMovieInfo(query);        // returns List<MovieDto>
            }
            case MUSIC -> {
                require(StringUtils.hasText(artist) && StringUtils.hasText(title),
                        "MUSIC requires artist and title");
                yield musicService.getMusicDetail(artist, title); // returns MusicResponse
            }
            case BOOK -> {
                require(StringUtils.hasText(title), "BOOK requires title");
                yield bookService.getBookDetail(title);        // returns BookResponse
            }
        };

        return ResponseEntity.ok(result);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    @GetMapping("/movie")
    public ResponseEntity<Map<String, Object>> getContents(@RequestParam("testId") Long testId) {
        List<Movie> updatedList = movieService.updateMovieFromTitle(testId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updatedList", updatedList);  // 기존 DB 기반 업데이트된 전체
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/book", params = "book")
    public ResponseEntity getBookDetail(@RequestParam("book") String book) {
        List<String> bookList = splitter(book);
        return handleBooks(bookList);
    }

    @GetMapping(value = "/music", params = "music")
    public ResponseEntity getMusicDetail(@RequestParam("music") String music) {
        List<String> musicList = splitter(music);
        return handleMusic(musicList);
    }

    @GetMapping(value = "/music", params = "testId")
    public ResponseEntity getMusicByTestId(@RequestParam("testId") Long testId) {
        try {
            return ResponseEntity.ok(musicService.getMusicWithTestId(testId));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/book", params = "testId")
    public ResponseEntity getBooksByTestId(@RequestParam("testId") Long testId) {
        try {
            return ResponseEntity.ok(bookService.getBooksWithTestId(testId));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    private List<String> splitter(String joined) {
        return Arrays.stream(joined.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private ResponseEntity handleBooks(List<String> bookList) {
        try {
            List<BookResponse> responses = new ArrayList<>();
            for (String book : bookList) {
                responses.add(bookService.getBookDetail(book));
            }

            List<BookDto> dtos = responses.stream().map(BookDto::from).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    private ResponseEntity handleMusic(List<String> musicList) {
        List<String> artistList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();

        for (String m : musicList) {
            String[] tmp = m.split(ARTIST_TITLE_DELIMITER_REGEX, 2);
            if (tmp.length == 2) {
                artistList.add(tmp[0].trim());
                titleList.add(tmp[1].trim());
            }
        }
        if (artistList.isEmpty() || artistList.size() != titleList.size()) {
            return ResponseEntity.badRequest().body("Invalid format. Use 'artist-title'");
        }

        try {
            List<MusicResponse> responses = new ArrayList<>();
            for (int i = 0; i < artistList.size(); i++) {
                responses.add(musicService.getMusicDetail(artistList.get(i), titleList.get(i)));
            }

            List<MusicDto> dtos = responses.stream().map(MusicDto::from).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}