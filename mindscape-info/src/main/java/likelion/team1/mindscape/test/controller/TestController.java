package likelion.team1.mindscape.test.controller;

import likelion.team1.mindscape.test.dto.TestRequestDto;
import likelion.team1.mindscape.test.dto.TestResponseDto;
import likelion.team1.mindscape.test.dto.TestResponseSimpleDto;
import likelion.team1.mindscape.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    //테스트 저장
    @PostMapping("/save")
    public ResponseEntity<Map<String, Long>> saveTest(@RequestParam("id") Long userId,
                                                      @RequestBody TestRequestDto dto) {
        dto.setUserId(userId);  // 직접 주입
        Long testId = testService.saveTest(dto);
        return ResponseEntity.ok(Map.of("testId", testId));
    }

    //  마이페이지: 내 테스트 히스토리
    @GetMapping("/history")
    public ResponseEntity<List<TestResponseDto>> getHistory(@RequestParam("id") Long userId) {
        List<TestResponseDto> history = testService.getTestHistory(userId);
        return ResponseEntity.ok(history);
    }


    @GetMapping("/internal/tests/{testId}")
    public ResponseEntity<TestResponseSimpleDto> getTestInfo(@PathVariable Long testId) {
        TestResponseSimpleDto response = testService.getTestInfoById(testId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getTestIdsByUser(@RequestParam("id") Long userId) {
        List<Long> testIds = testService.getTestIdsByUserId(userId);
        return ResponseEntity.ok(testIds);
    }

}
