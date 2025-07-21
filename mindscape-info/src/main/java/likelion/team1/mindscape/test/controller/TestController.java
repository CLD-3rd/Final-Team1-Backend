package likelion.team1.mindscape.test.controller;

import likelion.team1.mindscape.test.dto.TestRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import likelion.team1.mindscape.test.dto.TestResponseDto;
import likelion.team1.mindscape.test.dto.TestResponseSimpleDto;
import likelion.team1.mindscape.test.service.TestService;


@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/save")
    public ResponseEntity<String> saveTest(@RequestBody TestRequestDto dto) {
        testService.saveTest(dto);
        return ResponseEntity.ok("Test saved successfully");
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<TestResponseDto>> getHistory(@RequestParam Long userId) {
        List<TestResponseDto> history = testService.getTestHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/internal/tests/{testId}")
    public ResponseEntity<TestResponseSimpleDto> getTestInfo(@PathVariable Long testId) {
        TestResponseSimpleDto response = testService.getTestInfoById(testId);
        return ResponseEntity.ok(response);
    }
}
