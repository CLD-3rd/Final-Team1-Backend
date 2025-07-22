package likelion.team1.mindscape.content.service;
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
    private final RedisService redisService;
    // 1) 콘텐츠 저장
    public void saveRecomContent(Long userId, Long testId, String contentType, List<String> titles) {
        String redisKey = redisService.makeRecomKey(userId, testId, contentType);
        redisTemplate.delete(redisKey); // 기존 데이터 제거 (덮어쓰기)
        redisTemplate.opsForList().rightPushAll(redisKey, titles.toArray());
    }
    // 2) 콘텐츠 조회
    public List<String> getRecomContent(Long userId, Long testId, String contentType) {
        String redisKey = redisService.makeRecomKey(userId, testId, contentType);
        Long size = redisTemplate.opsForList().size(redisKey);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }
        return redisTemplate.opsForList().range(redisKey, 0, size - 1)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}

