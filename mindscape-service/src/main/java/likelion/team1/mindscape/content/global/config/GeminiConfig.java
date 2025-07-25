package likelion.team1.mindscape.content.global.config;

import com.google.genai.Client;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model.name}")
    private String modelName;

    @Value("${gemini.prompt.systemInstruction:}") // systemInstruction은 선택적
    private String systemInstruction;

    @Bean
    public Client geminiClient() {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    // 1.0.0 버전에는 GenerateContentConfig가 없으므로 해당 빈은 삭제하거나 주석 처리
}