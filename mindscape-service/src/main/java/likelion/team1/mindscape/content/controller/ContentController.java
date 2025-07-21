package likelion.team1.mindscape.content.controller;
import likelion.team1.mindscape.content.dto.response.content.BookResponse;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MusicResponse;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.service.BookService;
import likelion.team1.mindscape.content.service.MovieService;
import likelion.team1.mindscape.content.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/content")
public class ContentController {
    private final MovieService movieService;
    private final BookService bookService;
    private final MusicService musicService;

    @GetMapping("/movie")
    public ResponseEntity<Movie> getContents(@RequestParam String query, @RequestParam("userId") Long userId) {
        List<MovieDto> dto = movieService.getMovieInfo(query);
        Movie saved = movieService.saveMovie(dto, userId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/book/{bookTitle}")
    public ResponseEntity getBookDetail(@PathVariable String bookTitle) {
        try {
            BookResponse bookResponse = bookService.getBookDetails(bookTitle);
            return ResponseEntity.ok(bookResponse);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/test/{artist}/{title}")
    public ResponseEntity getMusicDetail(@PathVariable String artist, @PathVariable String title) {
        try {
            MusicResponse musicResponse = musicService.getMusicDetails(artist, title);
            return ResponseEntity.ok(musicResponse);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
