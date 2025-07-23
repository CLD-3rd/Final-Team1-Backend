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

    @GetMapping("/movie")
    public ResponseEntity<Map<String, Object>> getContents(@RequestParam("userId") Long userId, @RequestParam("recomId") Long recomId) {
        List<Movie> updatedList = movieService.updateMovieFromTitle(userId, recomId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updatedList", updatedList);  // 기존 DB 기반 업데이트된 전체
        return ResponseEntity.ok(result);
    }

    @GetMapping("/book/{bookTitles:.+}")
    public ResponseEntity getBookDetail(@PathVariable("bookTitles") String bookTitles) {
        List<String> titleList = Arrays.stream(bookTitles.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        //temporary
        Long userId = 1L;

        try {
            // 1. call Kakao api
            List<BookResponse> responses = (titleList.size() == 1) ?
                    // one book
                    List.of(bookService.getBookDetail(titleList.get(0))) :
                    // multiple books
                    bookService.getBooksDetails(titleList);

            // 2. bookresponse -> dto
            List<BookDto> dtos = responses.stream().map(BookDto::from).collect(Collectors.toList());

            // 3. save
            List<Book> saved = bookService.saveBook(dtos, userId);
            bookService.saveBookToRedis(dtos);
            // 4. return
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/music/{music:.+}")
    public ResponseEntity getMusicDetail(@PathVariable("music") String music) {
        List<String> musicList = Arrays.stream(music.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        //temporary
        Long userId = 1L;

        List<String> artistList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();

        for (String m : musicList) {
            String[] tmp = m.split("[–-]", 2);   // allow en‑dash(–) or hyphen(-)
            if (tmp.length == 2) {
                artistList.add(tmp[0].trim());
                titleList.add(tmp[1].trim());
            }
        }

        if (artistList.isEmpty() || artistList.size() != titleList.size()) {
            return ResponseEntity.badRequest().body("Invalid format. Use 'artist-title'");
        }

        try {
            // 1. call lastfm api
            List<MusicResponse> responses = (titleList.size() == 1) ?
                    // one music
                    List.of(musicService.getMusicDetail(artistList.get(0), titleList.get(0))) :
                    // multiple music
                    musicService.getMusicDetails(artistList, titleList);

            // 2. bookresponse -> dto
            List<MusicDto> dtos = responses.stream().map(MusicDto::from).collect(Collectors.toList());

            // 3. save
            List<Music> saved = musicService.saveMusic(dtos, userId);

            //4. save to redis
            musicService.saveMusicToRedis(dtos);

            //5. return
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/music")
    public ResponseEntity getMusicByTestId(@RequestParam("testId") String testId) {
        try {
            return ResponseEntity.ok(musicService.getMusicWithTestId(Long.valueOf(testId)));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/book")
    public ResponseEntity getBooksByTestId(@RequestParam("testId") String testId) {
        try {
            return ResponseEntity.ok(bookService.getBooksWithTestId(Long.valueOf(testId)));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
