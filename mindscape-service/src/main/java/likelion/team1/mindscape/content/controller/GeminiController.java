package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import likelion.team1.mindscape.content.dto.response.GeminiResponse;
import likelion.team1.mindscape.content.service.GeminiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GeminiController {
	private final GeminiService geminiService;
	private final ContentService contentService;
	
	//front에서 testid를 받아오는 것
	@PostMapping("/api/gemini/recommend")
	public ResponseEntity<GeminiResponse> recommend(@RequestParam Long testId) {
		GeminiResponse response = geminiService.recommend(testId);
		contentService.saveAllRecomContent(testId, response);
		return ResponseEntity.ok(response);  // recomId 없이 응답만 OK 처리
	}

}
