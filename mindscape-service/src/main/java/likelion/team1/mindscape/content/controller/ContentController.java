package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.dto.response.MusicResponse;
import likelion.team1.mindscape.content.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class ContentController {
    private final MusicService musicService;

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