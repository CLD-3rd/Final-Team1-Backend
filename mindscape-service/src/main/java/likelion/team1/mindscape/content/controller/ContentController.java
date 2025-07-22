package likelion.team1.mindscape.content.controller;
import likelion.team1.mindscape.content.dto.response.content.BookResponse;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.service.BookService;
import likelion.team1.mindscape.content.service.MovieService;
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

    @GetMapping("/movie")
    public ResponseEntity<Movie> getContents(@RequestParam String query, @RequestParam("userId") Long userId) {
        List<MovieDto> dto = movieService.getMovieInfo(query);
        Movie saved = movieService.saveMovieToDB(dto, userId);
        movieService.saveMovieToRedis(dto);
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
}
