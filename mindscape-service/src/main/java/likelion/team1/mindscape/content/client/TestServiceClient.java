package likelion.team1.mindscape.content.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import likelion.team1.mindscape.content.dto.response.TestInfoResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



@Component
public class TestServiceClient {

     private final RestTemplate restTemplate = new RestTemplate();

     @Value("${info.app.url}")
     private String infoAppUrl;

    // 주소 정확히 입력해야함
    public TestInfoResponse getTestInfo(Long testId) {
        // 실제 호출은 주석 처리
        // String url = "http://localhost:8081/internal/tests/" + testId;
        // return restTemplate.getForObject(url, TestInfoResponse.class);

    	//----------------------------------------------------
        // 임시 하드코딩 (testId=1일 때 테스트용)!!!!!!!!!!!!
    	return new TestInfoResponse(1L, 101L, "I");

    }

    public List<Long> getTestIdsByUserId(Long userId) {
        String url = infoAppUrl + "/api/test/ids?id=" + userId;
        Long[] testIds = restTemplate.getForObject(url, Long[].class);
        return testIds != null ? Arrays.asList(testIds) : Collections.emptyList();
    }
}