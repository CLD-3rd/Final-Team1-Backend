package likelion.team1.mindscape.content.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import likelion.team1.mindscape.content.dto.response.TestInfoResponse;



@Component
public class TestServiceClient {

    private final RestTemplate restTemplate;
    private final String testServiceUrl = "http://localhost:8080/api/test/internal/tests/";  // 테스트 서버 API 기본 URL

    public TestServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TestInfoResponse getTestInfo(Long testId) {
        String url = testServiceUrl + testId;
        return restTemplate.getForObject(url, TestInfoResponse.class);
    }
}