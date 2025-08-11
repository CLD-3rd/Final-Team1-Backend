package likelion.team1.mindscape.content.client;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
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

        int maxRetries = 3;
        int attempt = 0;
        RuntimeException lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;

                ResponseStream<GenerateContentResponse> responseStream = geminiClient.models.generateContentStream(modelName, content, null);

                StringBuilder responseContent = new StringBuilder();
                responseStream.forEach(response -> responseContent.append(response.text()));

                System.out.println("\n\n=== API Response Start ===\n" + responseContent.toString() + "\n=== API Response End ===\n\n");

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

                // 여기서 간단한 문자열 정제 시도
                // 예: 콤마 뒤에 % 같은 특수문자 제거 또는 잘못된 따옴표 교정
                jsonString = jsonString.replaceAll(",\\s*['\"]?%['\"]?", ",");

                JSONObject resultJson = new JSONObject(jsonString);

                List<String> movie = resultJson.getJSONArray("movie").toList().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

                List<String> book = resultJson.getJSONArray("book").toList().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

                List<String> music = resultJson.getJSONArray("music").toList().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

                return new GeminiResponse(movie, book, music);

            } catch (Exception e) {
                lastException = new RuntimeException("Gemini 응답 파싱 실패 시도 #" + attempt, e);
                System.err.println(lastException.getMessage());
                // 잠시 대기 후 재시도 (옵션)
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }

        // 재시도 실패 시 예외 던짐
        throw lastException;
    }
}