package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class ContentController {
    private final ContentService contentService;

    @GetMapping("/test")
    public ResponseEntity test() {
        return ResponseEntity.ok(contentService.getBookDetails("해리 포터와 마법사의 돌"));
    }
}
