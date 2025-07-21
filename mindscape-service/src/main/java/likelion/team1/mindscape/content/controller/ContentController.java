package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.service.MovieService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/content")
public class ContentController {
    private final MovieService movieService;

    @GetMapping("/movie")
    public ResponseEntity<Movie> getContents(@RequestParam String query, @RequestParam("userId") Long userId) {
        List<MovieDto> dto = movieService.getMovieInfo(query);
        Movie saved = movieService.saveMovie(dto, userId);
        return ResponseEntity.ok(saved);
    }
}
