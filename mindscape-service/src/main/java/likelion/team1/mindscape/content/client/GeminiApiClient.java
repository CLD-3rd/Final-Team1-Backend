package likelion.team1.mindscape.content.client;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import likelion.team1.mindscape.content.dto.response.GeminiResponse;
import likelion.team1.mindscape.content.global.config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GeminiApiClient {

	private final Client geminiClient;
    private final GeminiConfig geminiConfig;

    public GeminiResponse getRecommendations(String prompt) {
        // 1. 모델 이름 가져오기
        String modelName = geminiConfig.getModelName();

        // 2. 프롬프트를 Content 객체로 변환
        var content = Content.fromParts(Part.fromText(prompt));

        // 3. Gemini API 스트림 호출
        ResponseStream<GenerateContentResponse> responseStream = geminiClient.models.generateContentStream(modelName, content, null);


        // 4. 스트림에서 텍스트만 추출해 누적
        
        StringBuilder responseContent = new StringBuilder();
        responseStream.forEach(response -> {
            // response는 GenerateContentResponse 타입이고 text() 메서드로 텍스트 추출 가능
            responseContent.append(response.text());
        });

        // 5. 응답 로그 출력 (앞뒤 구분선 포함)
        System.out.println("\n\n=== API Response Start ===\n" + responseContent.toString() + "\n=== API Response End ===\n\n");

        // 6. JSON 부분만 추출
        String rawResponse = responseContent.toString()
            .replaceAll("```json", "")
            .replaceAll("```", "")
            .trim();

        int jsonStart = rawResponse.indexOf("{");
        int jsonEnd = rawResponse.lastIndexOf("}") + 1;

        if (jsonStart < 0 || jsonEnd < 0 || jsonStart >= jsonEnd) {
            throw new RuntimeException("응답에서 JSON을 찾을 수 없습니다.");
        }

        String jsonString = rawResponse.substring(jsonStart, jsonEnd);

        // 7. JSON 파싱
        JSONObject resultJson = new JSONObject(jsonString);

        // 8. 영화, 책, 음악 리스트 추출
        List<String> movie = resultJson.getJSONArray("movie").toList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> book = resultJson.getJSONArray("book").toList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> music = resultJson.getJSONArray("music").toList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // 9. GeminiResponse 반환
        return new GeminiResponse(movie, book, music);
    }
}
