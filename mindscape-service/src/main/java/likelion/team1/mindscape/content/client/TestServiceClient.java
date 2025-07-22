package likelion.team1.mindscape.content.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import likelion.team1.mindscape.content.dto.response.TestInfoResponse;



@Component
public class TestServiceClient {

    // private final RestTemplate restTemplate = new RestTemplate();
    
    // 주소 정확히 입력해야함
    public TestInfoResponse getTestInfo(Long testId) {
        // 실제 호출은 주석 처리
        // String url = "http://localhost:8081/internal/tests/" + testId;
        // return restTemplate.getForObject(url, TestInfoResponse.class);

    	//----------------------------------------------------
        // 임시 하드코딩 (testId=1일 때 테스트용)!!!!!!!!!!!!
    	return new TestInfoResponse(1L, 101L, "I");

    }
}