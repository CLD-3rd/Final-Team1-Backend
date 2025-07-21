package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.dto.response.BookResponse;
import likelion.team1.mindscape.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

@RequiredArgsConstructor
@RestController
public class ContentController {
    private final ContentService contentService;

    @GetMapping("/test/{bookTitle}")
    public ResponseEntity getBookDetail(@PathVariable String bookTitle) {
        try {
            BookResponse bookResponse = contentService.getBookDetails(bookTitle);
            return ResponseEntity.ok(bookResponse);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
