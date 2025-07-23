package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.dto.response.content.*;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.entity.Music;
import likelion.team1.mindscape.content.service.BookService;
import likelion.team1.mindscape.content.service.MovieService;
import likelion.team1.mindscape.content.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/movie")
    public ResponseEntity<Map<String, Object>> getContents(@RequestParam("userId") Long userId, @RequestParam("recomId") Long recomId) {
        List<Movie> updatedList = movieService.updateMovieFromTitle(userId, recomId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updatedList", updatedList);  // 기존 DB 기반 업데이트된 전체
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/book", params = "book")
    public ResponseEntity getBookDetail(@RequestParam("book") String book) {
        List<String> bookList = splitter(book);
        Long userId = 1L; // TODO: temporary
        return handleBooks(bookList, userId);
    }

    @GetMapping(value = "/music", params = "music")
    public ResponseEntity getMusicDetail(@RequestParam("music") String music) {
        List<String> musicList = splitter(music);
        Long userId = 1L; // TODO: temp.
        return handleMusic(musicList, userId);
    }

    @GetMapping(value = "/music", params = "testId")
    public ResponseEntity getMusicByTestId(@RequestParam("testId") String testId) {
        try {
            return ResponseEntity.ok(musicService.getMusicWithTestId(Long.valueOf(testId)));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/book", params = "testId")
    public ResponseEntity getBooksByTestId(@RequestParam("testId") String testId) {
        try {
            return ResponseEntity.ok(bookService.getBooksWithTestId(Long.valueOf(testId)));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/redis-init")
    public void redisInit() {
        //Dummy data set
        List<String> moviesList = List.of("쇼생크 탈출", "인셉션", "매트릭스");
        List<String> booksList = List.of("앵무새 죽이기", "1984", "연금술사");
        List<String> musicList = List.of("Queen - Bohemian Rhapsody", "The Beatles - Let It Be", "Bob Dylan - Like A Rolling Stone");

        List<MovieDto> movieDtos = new ArrayList<>();
        List<MusicDto> musicDtos = new ArrayList<>();
        List<BookDto> bookDtos = new ArrayList<>();

        // get details
        try {
            for (int i = 0; i < moviesList.size(); i++) {
                movieDtos.add(movieService.getMovieInfo(moviesList.get(i)).get(0));
                bookDtos.add(BookDto.from(bookService.getBookDetail(booksList.get(i))));

                String[] tmp = musicList.get(i).split("[–-]", 2);
                musicDtos.add(MusicDto.from(musicService.getMusicDetail(tmp[0].trim(), tmp[1].trim())));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (MovieDto movieDto : movieDtos) {
            List<MovieDto> tmp = List.of(movieDto);
            movieService.saveMovieToRedis(tmp);
        }
        musicService.saveMusicToRedis(musicDtos);
        bookService.saveBookToRedis(bookDtos);
    }


    private List<String> splitter(String joined) {
        return Arrays.stream(joined.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private ResponseEntity handleBooks(List<String> bookList, Long userId) {
        try {
            List<BookResponse> responses = (bookList.size() == 1)
                    ? List.of(bookService.getBookDetail(bookList.get(0)))
                    : bookService.getBooksDetails(bookList);

            List<BookDto> dtos = responses.stream().map(BookDto::from).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    private ResponseEntity handleMusic(List<String> musicList, Long userId) {
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
            List<MusicResponse> responses = (titleList.size() == 1)
                    ? List.of(musicService.getMusicDetail(artistList.get(0), titleList.get(0)))
                    : musicService.getMusicDetails(artistList, titleList);

            List<MusicDto> dtos = responses.stream().map(MusicDto::from).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
