package likelion.team1.mindscape.content.service;
import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.GeminiResponse;
import likelion.team1.mindscape.content.dto.response.TestInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TestServiceClient testServiceClient;
    private final RedisService redisService;

    // 1) 콘텐츠 저장
    public void saveAllRecomContent(Long testId, GeminiResponse response) {

        TestInfoResponse testInfo = testServiceClient.getTestInfo(testId);
        Long userId = testInfo.getUserId();
        saveRecomContent(userId, testId, "movie", response.getMovie());
        saveRecomContent(userId, testId, "book", response.getBook());
        saveRecomContent(userId, testId, "music", response.getMusic());
    }
    public void saveRecomContent(Long userId, Long testId, String contentType, List<String> titles) {
        String redisKey = redisService.makeRecomKey(userId, testId, contentType);
        redisTemplate.delete(redisKey); // 기존 데이터 제거 (덮어쓰기)
        redisTemplate.opsForList().rightPushAll(redisKey, titles.toArray());
    }
}

